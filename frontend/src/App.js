import * as React from 'react'
import { Admin, Resource } from 'react-admin'
import { appConfig } from './appConfig'
import { getAuthProvider } from './jmix-ra/authProvider'
import { getDataProvider } from './jmix-ra/dataProvider'
import { getAuthorization } from './jmix-ra/authorization'
import { CategoryList, CategoryEdit, CategoryCreate } from "./app/category"
import { CurrencyCreate, CurrencyEdit, CurrencyList } from "./app/currency"
import { AccountCreate, AccountEdit, AccountList } from "./app/account"
import { OperationCreate, OperationEdit, OperationList } from "./app/operation"
import { Dashboard } from "./app/dashboard"

import OperationsIcon from "@material-ui/icons/Money"
import AccountIcon from "@material-ui/icons/AccountBalanceWallet"
import CurrencyIcon from "@material-ui/icons/AttachMoney"
import CategoryIcon from "@material-ui/icons/Category"

const App = () => {
    return (
        <Admin
            dataProvider={getDataProvider(appConfig)}
            authProvider={getAuthProvider(appConfig)}
            dashboard={Dashboard}>

            {permissions => {
                const auth = getAuthorization(permissions)
                return [
                    <Resource
                        name="akk_Operation-api"
                        options={{label: "Operations"}}
                        icon={OperationsIcon}
                        list={ auth.isReadPermitted("akk_Operation") ? OperationList : null }
                        edit={ auth.isUpdatePermitted("akk_Operation") ? OperationEdit : null }
                        create={ auth.isCreatePermitted("akk_Operation") ? OperationCreate : null }
                    />,

                    <Resource
                        name="akk_Account-api"
                        options={{label: "Accounts"}}
                        icon={AccountIcon}
                        list={ auth.isReadPermitted("akk_Account") ? AccountList : null }
                        edit={ auth.isUpdatePermitted("akk_Account") ? AccountEdit : null }
                        create={ auth.isCreatePermitted("akk_Account") ? AccountCreate : null }
                    />,

                    <Resource
                        name="akk_Category"
                        options={{label: "Categories"}}
                        icon={CategoryIcon}
                        list={ auth.isReadPermitted("akk_Category") ? CategoryList : null }
                        edit={ auth.isUpdatePermitted("akk_Category") ? CategoryEdit : null }
                        create={ auth.isCreatePermitted("akk_Category") ? CategoryCreate : null }
                    />,

                    <Resource
                        name="akk_Currency"
                        options={{label: "Currencies"}}
                        icon={CurrencyIcon}
                        list={ auth.isReadPermitted("akk_Currency") ? CurrencyList : null }
                        edit={ auth.isUpdatePermitted("akk_Currency") ? CurrencyEdit : null }
                        create={ auth.isCreatePermitted("akk_Currency") ? CurrencyCreate : null }
                    />
                ]
            }}
        </Admin>
    )
}

export default App;
