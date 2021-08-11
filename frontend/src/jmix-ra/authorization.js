
export const getAuthorization = (permissions) => {
    const p = getPermissionsObject(permissions)
    return {
        getAuthorities() {
            return p ? p.authorities : []
        },

        hasAuthority(authority) {
            return p ? p.authorities.includes(authority) : false
        },

        isEntityOperationPermitted(target) {
            return p ? p.entities && p.entities.find(it => it.target === target)?.value === 1 : false
        },

        isReadPermitted(entity) {
            return this.isEntityOperationPermitted(`${entity}:read`)
        },

        isCreatePermitted(entity) {
            return this.isEntityOperationPermitted(`${entity}:create`)
        },

        isUpdatePermitted(entity) {
            return this.isEntityOperationPermitted(`${entity}:update`)
        },

        isDeletePermitted(entity) {
            return this.isEntityOperationPermitted(`${entity}:delete`)
        },

        isSpecificsPermitted(target) {
            return p ? p.specifics && p.specifics.find(it => it.target === target)?.value === 1 : false
        }
    }
}

function getPermissionsObject(permissions) {
    if (!permissions) {
        return null
    }
    if (permissions.hasOwnProperty("loaded")) {
        if (!permissions.loaded) {
            return null
        } else {
            return permissions.hasOwnProperty("permissions") ? permissions.permissions : permissions
        }
    }
    return permissions
}
