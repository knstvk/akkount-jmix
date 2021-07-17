import * as React from 'react'
import { Admin, Resource, ListGuesser, EditGuesser } from 'react-admin'
import { jmixAuthProvider } from './jmix-ra/jmixAuthProvider'
import { jmixDataProvider } from './jmix-ra/jmixDataProvider'
// import { FooCreate, FooEdit, FooList } from './foo'
// import { BarList } from "./bar"

const App = () => {
  return (
      <Admin dataProvider={jmixDataProvider} authProvider={jmixAuthProvider}>
        <Resource name="akk_User" list={ListGuesser} />
        {/*<Resource name="Foo-api" options={{label: "Foo"}} list={FooList} edit={FooEdit} create={FooCreate} />*/}
        {/*<Resource name="Bar" list={BarList} />*/}
      </Admin>
  )
}

export default App;
