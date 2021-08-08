import React from "react"
import {
    List, Datagrid, TextField, SelectField,
    Edit, Create, SimpleForm, TextInput, SelectInput
} from "react-admin"

const categoryTypes = [
    {id: "EXPENSE", name: "Expense"},
    {id: "INCOME", name: "Income"}
]

export const CategoryList = (props) => (
    <List {...props} sort={{ field: 'name', order: 'ASC' }}>
        <Datagrid rowClick="edit">
            <TextField source="name" />
            <SelectField source="catType" choices={categoryTypes} />
        </Datagrid>
    </List>
)

export const CategoryEdit = (props) => (
    <Edit {...props}>
        <CategoryForm />
    </Edit>
)

export const CategoryCreate = (props) => (
    <Create {...props}>
        <CategoryForm />
    </Create>
)

const CategoryForm = (props) => (
    <SimpleForm {...props} redirect="list">
        <TextInput source="name"/>
        <SelectInput source="catType" choices={categoryTypes}/>
    </SimpleForm>
)
