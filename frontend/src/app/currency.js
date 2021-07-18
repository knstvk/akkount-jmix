import React from "react"
import {
    List, Datagrid, TextField,
    Edit, Create, SimpleForm, TextInput
} from "react-admin"

export const CurrencyList = props => (
    <List {...props}>
        <Datagrid rowClick="edit">
            <TextField source="code" />
            <TextField source="name" />
        </Datagrid>
    </List>
)

export const CurrencyEdit = props => (
    <Edit {...props}>
        <CurrencyForm />
    </Edit>
)

export const CurrencyCreate = props => (
    <Create {...props}>
        <CurrencyForm />
    </Create>
)

const CurrencyForm = props => (
    <SimpleForm {...props} redirect="list">
        <TextInput source="code" />
        <TextInput source="name" />
    </SimpleForm>
)