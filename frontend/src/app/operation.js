import React from "react"
// import { cloneElement } from "react"
import {
    List, Datagrid, TextField, NumberField, DateField, SelectField,
    TopToolbar, ExportButton,
    Edit, Create, SimpleForm, DateInput, NumberInput, ReferenceInput, SelectInput, TextInput
} from "react-admin"

import Button from "@material-ui/core/Button"
import { Link } from 'react-router-dom'

const opTypes = [
    {id: "EXPENSE", name: "Expense"},
    {id: "INCOME", name: "Income"},
    {id: "TRANSFER", name: "Transfer"}
]

const OperationCreateButton = (props) => (
    <Button
        component={Link}
        to={{
            pathname: '/akk_Operation-api/create',
            state: {
                record: {
                    opType: props.type
                }
            },
        }}
    >
        {opTypes.find(it => it.id === props.type).name}
    </Button>
)

const ListActions = (props) => (
    <TopToolbar>
        {/*{cloneElement(props.filters, { context: 'button' })}*/}
        <OperationCreateButton type="EXPENSE"/>
        <OperationCreateButton type="INCOME"/>
        <OperationCreateButton type="TRANSFER"/>
        {/*<CreateButton/>*/}
        <ExportButton/>
    </TopToolbar>
)

export const OperationList = (props) => (
    <List {...props} actions={<ListActions/>}>
        <Datagrid rowClick="edit">
            <SelectField source="opType" choices={opTypes} label="Type" />
            <DateField source="opDate" label="Date"/>
            <TextField source="acc1.name" label="Expense account" />
            <NumberField source="amount1" label="Expense" />
            <TextField source="acc2.name" label="Income account" />
            <NumberField source="amount2" label="Income" />
            <TextField source="category.name" label="Category"/>
            <TextField source="comments"/>
            <TextField source="createdBy"/>
        </Datagrid>
    </List>
)

export const OperationEdit = (props) => (
    <Edit {...props}>
        <SimpleForm>
            <SelectField source="opType" choices={opTypes} label="Type" />
            <DateInput source="opDate" label="Date" />
            <OpForm />
        </SimpleForm>
    </Edit>
)

export const OperationCreate = (props) => (
    <Create {...props}>
        <SimpleForm redirect="list">
            <SelectField source="opType" choices={opTypes} label="Type" />
            <DateInput source="opDate" label="Date" />
            <OpForm />
        </SimpleForm>
    </Create>
)

const OpForm = (props) => {
    if (!props.record) {
        return null
    }
    switch (props.record.opType) {
        case "EXPENSE": return (
            <>
                <ExpenseForm/>
                <div><CategoryInput/></div>
                <div><TextInput source="comments"/></div>
            </>
        )
        case "INCOME": return (
            <>
                <IncomeForm/>
                <div><CategoryInput/></div>
                <div><TextInput source="comments"/></div>
            </>
        )
        default: return (
            <>
                <ExpenseForm/>
                <IncomeForm/>
                <div><TextInput source="comments"/></div>
            </>
        )
    }
}

const ExpenseForm = (props) => (
    <>
        <div>
            <ReferenceInput reference="akk_Account-api" source="acc1.id" label="Expense account">
                <SelectInput optionText="name"/>
            </ReferenceInput>
        </div>
        <div>
            <NumberInput source="amount1" label="Expense"/>
        </div>
    </>
)

const IncomeForm = (props) => (
    <>
        <div>
            <ReferenceInput reference="akk_Account-api" source="acc2.id" label="Imcome account">
                <SelectInput optionText="name"/>
            </ReferenceInput>
        </div>
        <div>
            <NumberInput source="amount2" label="Income"/>
        </div>
    </>
)

const CategoryInput = (props) => (
    <ReferenceInput reference="akk_Category" source="category.id" label="Category">
        <SelectInput optionText="name" />
    </ReferenceInput>
)
