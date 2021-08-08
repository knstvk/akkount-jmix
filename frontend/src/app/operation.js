import React from "react"
import { cloneElement } from "react"
import {
    List, Datagrid, TextField, NumberField, DateField, SelectField, SimpleList,
    Filter,
    TopToolbar, ExportButton,
    Edit, Create, SimpleForm, DateInput, NumberInput, ReferenceInput, SelectInput, TextInput
} from "react-admin"

import Button from "@material-ui/core/Button"
import Box from "@material-ui/core/Box"
import Typography from "@material-ui/core/Typography"
import { useMediaQuery } from "@material-ui/core"

import { Link } from "react-router-dom"

const opTypes = [
    {id: "EXPENSE", name: "Expense"},
    {id: "INCOME", name: "Income"},
    {id: "TRANSFER", name: "Transfer"}
]

const getOpTypeName = (id) => {
    return opTypes.find(it => it.id === id).name
}

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
        {cloneElement(props.filters, { context: 'button' })}
        <OperationCreateButton type="EXPENSE"/>
        <OperationCreateButton type="INCOME"/>
        <OperationCreateButton type="TRANSFER"/>
        <ExportButton/>
    </TopToolbar>
)

const getSimpleOpTitle = (record) => {
    switch (record.opType) {
        case "EXPENSE": return `${getOpTypeName(record.opType)} from ${record.acc1.name} [${record.category?.name}] by ${record.createdBy}`
        case "INCOME": return `${getOpTypeName(record.opType)} to ${record.acc2.name} [${record.category?.name}] by ${record.createdBy}`
        case "TRANSFER": return `${getOpTypeName(record.opType)} from ${record.acc1.name} to ${record.acc2.name} by ${record.createdBy}`
        default: return getOpTypeName(record.opType)
    }
}

const getSimpleOpAmount = (record) => {
    switch (record.opType) {
        case "EXPENSE": return `${record.amount1} ${record.acc1.currency.code}`
        case "INCOME": return `${record.amount2} ${record.acc2.currency.code}`
        case "TRANSFER": return `${record.amount1} ${record.acc1.currency.code} → ${record.amount2} ${record.acc2.currency.code}`
        default: return "?"
    }
}

const OpFilter = props => (
    <Filter {...props}>
        <ReferenceInput source="acc1" label="From" reference="akk_Account-api" allowEmpty>
           <SelectInput optionText="name" />
        </ReferenceInput>
        <ReferenceInput source="acc2" label="To" reference="akk_Account-api" allowEmpty>
           <SelectInput optionText="name" />
        </ReferenceInput>
        <ReferenceInput source="category" label="Category" reference="akk_Category" allowEmpty>
           <SelectInput optionText="name" />
        </ReferenceInput>
        <TextInput label="Comments" source="comments" />
        <TextInput label="By" source="createdBy" />
    </Filter>
)

export const OperationList = (props) => {
    const isSmall = useMediaQuery(theme => theme.breakpoints.down('sm'))

    return (
        <List actions={<ListActions/>} empty={<EmptyContent/>}
              filters={<OpFilter/>} sort={{ field: 'opDate', order: 'DESC' }} {...props} >
            {
                isSmall ? (
                    <SimpleList
                        primaryText={record => getSimpleOpTitle(record)}
                        secondaryText={record => getSimpleOpAmount(record)}
                        tertiaryText={record => record.opDate}
                    />
                ) : (
                    <Datagrid rowClick="edit">
                        <SelectField source="opType" choices={opTypes} label="Type"/>
                        <DateField source="opDate" label="Date"/>
                        <TextField source="acc1.name" label="Expense account"/>
                        <NumberField source="amount1" label="Expense"/>
                        <TextField source="acc2.name" label="Income account"/>
                        <NumberField source="amount2" label="Income"/>
                        <TextField source="category.name" label="Category"/>
                        <TextField source="comments"/>
                        <TextField source="createdBy"/>
                    </Datagrid>
                )
            }
        </List>
    )
}

const EmptyContent = () => {
    return (
        <Box textAlign="center" m={1}>
            <Typography variant="h4" paragraph>
                No operations found
            </Typography>
            <Typography variant="body1">
                Create one
            </Typography>
            <OperationCreateButton type="EXPENSE"/>
            <OperationCreateButton type="INCOME"/>
            <OperationCreateButton type="TRANSFER"/>
        </Box>
    )
}

export const OperationEdit = (props) => (
    <Edit {...props}>
        <SimpleForm>
            <SelectField source="opType" choices={opTypes} label="Type" />
            <DateInput source="opDate" label="Date" />
            <OpForm />
        </SimpleForm>
    </Edit>
)

export const OperationCreate = (props) => {
    return (
        <Create {...props}>
            <SimpleForm redirect="list" initialValues={{ opDate: new Date() }}>
                <SelectField source="opType" choices={opTypes} label="Type"/>
                <DateInput source="opDate" label="Date"/>
                <OpForm/>
            </SimpleForm>
        </Create>
    )
}

const OpForm = (props) => {
    if (!props.record) {
        return null
    }
    switch (props.record.opType) {
        case "EXPENSE": return (
            <>
                <ExpenseForm/>
                <div>
                    <CategoryInput catType="EXPENSE"/>
                </div>
                <div>
                    <TextInput source="comments"/>
                </div>
            </>
        )
        case "INCOME": return (
            <>
                <IncomeForm/>
                <div>
                    <CategoryInput catType="INCOME"/>
                </div>
                <div>
                    <TextInput source="comments"/>
                </div>
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
            <ReferenceInput reference="akk_Account-api" source="acc2.id" label="Income account">
                <SelectInput optionText="name"/>
            </ReferenceInput>
        </div>
        <div>
            <NumberInput source="amount2" label="Income"/>
        </div>
    </>
)

const CategoryInput = (props) => (
    <ReferenceInput reference="akk_Category" source="category.id" label="Category"
                    filter={{catType: props.catType}}>
        <SelectInput optionText="name"/>
    </ReferenceInput>
)
