import * as React from 'react'
import { Admin, Resource, ListGuesser, EditGuesser } from 'react-admin'
import { jmixAuthProvider } from './jmix-ra/jmixAuthProvider'
import { jmixDataProvider } from './jmix-ra/jmixDataProvider'
import { CategoryList, CategoryEdit, CategoryCreate } from "./app/category"
import { CurrencyCreate, CurrencyEdit, CurrencyList } from "./app/currency"
import { AccountCreate, AccountEdit, AccountList } from "./app/account"
import { OperationCreate, OperationEdit, OperationList } from "./app/operation"
import { Dashboard } from "./app/dashboard"

const App = () => {
    return (
        <Admin dataProvider={jmixDataProvider} authProvider={jmixAuthProvider} dashboard={Dashboard}>
            <Resource name="akk_Operation-api" options={{label: "Operations"}} list={OperationList} edit={OperationEdit} create={OperationCreate}/>
            <Resource name="akk_Account-api" options={{label: "Accounts"}} list={AccountList} edit={AccountEdit} create={AccountCreate}/>
            <Resource name="akk_Category" options={{label: "Categories"}} list={CategoryList} edit={CategoryEdit} create={CategoryCreate}/>
            <Resource name="akk_Currency" options={{label: "Currencies"}} list={CurrencyList} edit={CurrencyEdit} create={CurrencyCreate}/>
        </Admin>
    )
}

export default App;
