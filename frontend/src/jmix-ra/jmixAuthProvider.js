
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
    },

    checkError: (error) => {
        const status = error.status;
        if (status === 401 || status === 403) {
            localStorage.removeItem('auth')
            return Promise.reject()
        }
        // other error code (404, 500, etc): no need to log out
        return Promise.resolve()
    },

    checkAuth: params => {
        return localStorage.getItem('auth')
            ? Promise.resolve()
            : Promise.reject()
    },

    logout: () => {
        localStorage.removeItem('auth')
        return Promise.resolve()
    },

    getIdentity: () => Promise.resolve(),

    getPermissions: params => Promise.resolve(),
}