import * as React from 'react'
import { Admin, Resource, ListGuesser, EditGuesser } from 'react-admin'
import { jmixAuthProvider } from './jmix-ra/jmixAuthProvider'
import { jmixDataProvider } from './jmix-ra/jmixDataProvider'
import { CategoryList, CategoryEdit, CategoryCreate } from "./category"

const App = () => {
    return (
        <Admin dataProvider={jmixDataProvider} authProvider={jmixAuthProvider}>
            <Resource name="akk_User" list={ListGuesser} />
            <Resource name="akk_Category" options={{label: "Category"}} list={CategoryList} edit={CategoryEdit} create={CategoryCreate}/>
        </Admin>
    )
}

export default App;
