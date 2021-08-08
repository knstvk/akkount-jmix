
export const jmixAuthProvider = {

    login: ({username, password}) => {
        return fetch('/oauth/token', {
            method: 'POST',
            headers: {
                'Authorization': 'Basic ' + btoa('client:secret'),
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `grant_type=password&username=${username}&password=${password}`
        })
        .then(response => {
            if (response.status < 200 || response.status >= 300) {
                throw new Error(response.statusText)
            }
            return response.json()
        })
        .then(auth => {
            localStorage.setItem('auth', JSON.stringify(auth))
        })
        .then(() => {
            const {access_token} = JSON.parse(localStorage.getItem("auth"))
            return fetch("/rest/permissions", {
                headers: {
                    "Authorization": `Bearer ${access_token}`
                }
            })
        })
        .then(response => {
            if (response.ok) {
                return response.json()
            } else {
                return Promise.reject("cannot get permissions")
            }
        })
        .then(permissions => {
            console.log(">>> set permissions:", permissions)
            localStorage.setItem("permissions", JSON.stringify(permissions))
        })
    },

    checkError: (error) => {
        const status = error.status;
        if (status === 401) {
            localStorage.removeItem('auth')
            return Promise.reject()
        }
        if (status === 403) {
            return Promise.reject({ logoutUser: false })
        }
        // other error code (404, 500, etc): no need to log out
        return Promise.resolve()
    },

    checkAuth: (params) => {
        return localStorage.getItem('auth')
            ? Promise.resolve()
            : Promise.reject()
    },

    logout: () => {
        localStorage.removeItem('auth')
        return Promise.resolve()
    },

    getIdentity: () => Promise.resolve(),

    getPermissions: (params) => {
        const permissions = localStorage.getItem("permissions")
        return permissions ? Promise.resolve(JSON.parse(permissions)) : Promise.reject()
    },
}