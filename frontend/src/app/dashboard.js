import React from "react"
import Card from "@material-ui/core/Card"
import CardContent from "@material-ui/core/CardContent"
import { Title } from "react-admin"
import { useState, useEffect } from "react"

const fetchBalance = () => {
    console.debug("fetching balance")
    const auth = localStorage.getItem("auth")
    if (auth) {
        const {access_token} = JSON.parse(auth)
        return fetch("/rest/services/akk_BalanceService/getBalanceData?date=2021-07-25", {
            headers: {
                "Authorization": `Bearer ${access_token}`
            }
        })
            .then((response) => {
                if (response.ok) {
                    return response.json()
                } else {
                    return Promise.reject(response.status)
                }
            })
    } else {
        return Promise.reject("not authenticated")
    }
}

export const Dashboard = () => {

    const [ balanceData, setBalanceData ] = useState([])
    const [ hasError, setHasError ] = useState(false)

    useEffect(() => {
        fetchBalance()
        .then((data) => {
            // console.log(data)
            setBalanceData(data)
        })
        .catch((err) => {
            console.log("Fetch error:", err)
            setHasError(true)
        })
    }, [])

    return (
        <>
            <Title title="Balance"/>
            <div>
                { hasError ? <div>Cannot fetch balance</div> : balanceData.map((groupData) => <BalanceGroup data={groupData}/>)}
            </div>
        </>
    )
}

const BalanceGroup = (props) => (
    <Card style={{marginBottom: "12px"}}>
        <CardContent>
            <table>
                <tbody>
                    {props.data.accounts.map(value => <AccountRow data={value}/>)}
                    {props.data.totals.map(value => <TotalRow data={value}/>)}
                </tbody>
            </table>
        </CardContent>
    </Card>
)

const numFormat = new Intl.NumberFormat("en-US", {maximumFractionDigits: 0})

const AccountRow = (props) => {
    return (
        <tr key={props.data.name}>
            <td>{props.data.name}</td>
            <td style={{textAlign: "right", padding: "0 5px 0 10px"}}>{numFormat.format(props.data.amount)}</td>
            <td style={{paddingRight: "4px"}}>{props.data.currency}</td>
        </tr>
    )
}

const TotalRow = (props) => {
    return (
        <tr key={props.data.currency} style={{backgroundColor: "rgb(240, 242, 245)", paddingRight: "5px"}}>
            <td/>
            <td style={{textAlign: "right", padding: "0 5px 0 10px", fontWeight: "bold"}}>{numFormat.format(props.data.amount)}</td>
            <td style={{paddingRight: "4px", fontWeight: "bold"}}>{props.data.currency}</td>
        </tr>
    )
}
