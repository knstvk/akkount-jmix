import { fetchUtils } from "react-admin"
import { stringify } from "query-string"
import { normalizeEntity, normalizeEntities } from "./jmixEntityNormalizer"

const apiUrl = "/rest/entities"

const httpClient = (url, options = {}) => {
    if (!options.headers) {
        options.headers = new Headers({Accept: "application/json"})
    }
    const {access_token} = JSON.parse(localStorage.getItem("auth"))
    options.headers.set("Authorization", `Bearer ${access_token}`)
    return fetchUtils.fetchJson(url, options)
}

const createFilter = (raFilter) => {
    const entries = Object.entries(raFilter)
    if (!entries.length) {
        return null
    } else {
        const jmixFilter = {
            conditions: entries.map(entry => (
                {
                    property: entry[0],
                    operator: "=",
                    value: entry[1]
                }
            ))
        }
        return jmixFilter
    }
}

const getEntityAndFetchPlan = (resource) => {
    const sepIdx = resource.indexOf("-")
    return {
        entity: sepIdx > -1 ? resource.slice(0, sepIdx) : resource,
        fetchPlan: sepIdx > -1 ? resource.slice(sepIdx + 1) : null
    }
}

export const jmixDataProvider = {

    getList: (resource, params) => {
        console.debug("getList:", resource, params)

        const { entity, fetchPlan } = getEntityAndFetchPlan(resource)

        const {page, perPage} = params.pagination
        const {field, order} = params.sort
        const query = {
            returnCount: true,
            sort: `${order === "ASC" ? "" : "-"}${field}`,
            limit: perPage,
            offset: (page - 1) * perPage,
        }
        if (fetchPlan) {
            query.fetchPlan = fetchPlan
        }

        let url = `${apiUrl}/${entity}`
        let options

        const filter = createFilter(params.filter)
        if (filter) {
            url = url + "/search"
            options = {
                method: "POST",
                body: JSON.stringify({
                    ...query,
                    filter: filter
                })
            }
        } else {
            url = url + "?" + stringify(query)
        }

        return httpClient(url, options)
            .then(({headers, json}) => ({
                data: normalizeEntities(json),
                total: parseInt(headers.get("X-Total-Count"), 10),
            }))
    },

    getOne: (resource, params) => {
        console.debug("getOne:", resource, params)

        const { entity, fetchPlan } = getEntityAndFetchPlan(resource)
        const query = {}
        if (fetchPlan) {
            query.fetchPlan = fetchPlan
        }

        return httpClient(`${apiUrl}/${entity}/${params.id}?${stringify(query)}`)
            .then(({json}) => ({
                data: normalizeEntity(json),
            }))
    },

    getMany: (resource, params) => {
        console.debug("getMany:", resource, params)

        const { entity, fetchPlan } = getEntityAndFetchPlan(resource)

        const url = `${apiUrl}/${entity}/search`

        const query = fetchPlan ? { fetchPlan: fetchPlan } : {}

        const filter = {
            conditions: [
                {
                    property: "id",
                    operator: "in",
                    value: params.ids
                }
            ]
        }

        const options = {
            method: "POST",
            body: JSON.stringify({
                ...query,
                filter: filter
            })
        }

        return httpClient(url, options)
            .then(({headers, json}) => ({
                data: normalizeEntities(json)
            }))
    },

    getManyReference: (resource, params) => {
        console.debug("getManyReference:", resource, params)

        const {page, perPage} = params.pagination
        const {field, order} = params.sort
        const query = {
            sort: JSON.stringify([field, order]),
            range: JSON.stringify([(page - 1) * perPage, page * perPage - 1]),
            filter: JSON.stringify({
                ...params.filter,
                [params.target]: params.id,
            }),
        }
        const url = `${apiUrl}/${resource}?${stringify(query)}`

        return httpClient(url).then(({headers, json}) => ({
            data: json,
            total: parseInt(headers.get("content-range").split("/").pop(), 10),
        }))
    },

    update: (resource, params) => {
        console.debug("update:", resource, params)

        const { entity, fetchPlan } = getEntityAndFetchPlan(resource)
        const query = {}
        if (fetchPlan) {
            query.responseFetchPlan = fetchPlan
        }

        return httpClient(`${apiUrl}/${entity}/${params.id}?${stringify(query)}`, {
            method: "PUT",
            body: JSON.stringify(params.data),
        }).then(({json}) => ({data: json}))
    },

    updateMany: (resource, params) => {
        console.debug("updateMany:", resource, params)

        const query = {
            filter: JSON.stringify({id: params.ids}),
        }
        return httpClient(`${apiUrl}/${resource}?${stringify(query)}`, {
            method: "PUT",
            body: JSON.stringify(params.data),
        }).then(({json}) => ({data: json}))
    },

    create: (resource, params) => {
        console.debug("create:", resource, params)

        const { entity, fetchPlan } = getEntityAndFetchPlan(resource)
        const query = {}
        if (fetchPlan) {
            query.responseFetchPlan = fetchPlan
        }

        return httpClient(`${apiUrl}/${entity}?${stringify(query)}`, {
            method: "POST",
            body: JSON.stringify(params.data),
        }).then(({json}) => ({
            data: {...params.data, id: json.id},
        }))
    },

    delete: (resource, params) => {
        console.debug("delete:", resource, params)

        const { entity } = getEntityAndFetchPlan(resource)

        return httpClient(`${apiUrl}/${entity}/${params.id}`, {
                method: "DELETE",
            }).then(() => ({data: params.previousData}))
    },

    deleteMany: (resource, params) => {
        console.debug("deleteMany:", resource, params)

        const { entity } = getEntityAndFetchPlan(resource)

        return httpClient(`${apiUrl}/${entity}`, {
            method: "DELETE",
            body: JSON.stringify(params.ids),
        }).then(() => ({data: params.ids}))
    },
}