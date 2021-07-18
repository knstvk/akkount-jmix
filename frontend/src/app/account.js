import React from "react"
import {
    List, Datagrid, TextField, BooleanField, NumberField,
    Edit, Create, SimpleForm, TextInput, BooleanInput, NumberInput, ReferenceInput, SelectInput
} from "react-admin"

export const AccountList = (props) => (
    <List {...props}>
        <Datagrid rowClick="edit">
            <TextField source="name" />
            <TextField source="description" />
            <TextField source="currency.code" label="Currency" />
            <NumberField source="group" />
            <BooleanField source="active" />
        </Datagrid>
    </List>
)

export const AccountEdit = (props) => (
    <Edit {...props}>
        <AccountForm />
    </Edit>
)

export const AccountCreate = (props) => (
    <Create {...props}>
        <AccountForm />
    </Create>
)

const AccountForm = (props) => (
    <SimpleForm {...props} redirect="list">
        <TextInput source="name" />
        <TextInput source="description" />
        <ReferenceInput source="currency.id" reference="akk_Currency" label="Currency">
            <SelectInput optionText="code" />
        </ReferenceInput>
        <NumberInput source="group" />
        <BooleanInput source="active" />
    </SimpleForm>
)