
export const normalizeEntities = (entities) => {
    return entities.map(entity => normalizeEntity(entity))
}

export const normalizeEntity = (entity) => {
    delete entity["_entityName"]
    delete entity["_instanceName"]

    const props = Object.keys(entity)
    for (const prop of props) {
        const val = entity[prop]
        if (typeof val === "object") {
            if (Array.isArray(val)) {
                for (const valItem of val) {
                    normalizeEntity(valItem)
                }
            } else {
                entity[prop] = normalizeEntity(val)
            }
        }
    }

    return entity
}
