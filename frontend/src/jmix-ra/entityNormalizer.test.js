import { normalizeEntities } from "./entityNormalizer"

const customer1 = {
    _entityName: "Customer",
    _instanceName: "customer-1",
    name: "cust1"
}

const line1 = {
    _entityName: "Line",
    _instanceName: "line-1",
    product: "abc",
    qty: 10
}

const line2 = {
    _entityName: "Line",
    _instanceName: "line-2",
    product: "def",
    qty: 20
}

const order1 = {
    _entityName: "Order",
    _instanceName: "order-1",
    num: 111,
    customer: customer1,
    lines: [line1, line2]
}

test("test removing system attributes", () => {
    expect(normalizeEntities([order1])).toStrictEqual([
        {
            num: 111,
            customer: {
                name: "cust1"
            },
            lines: [
                {
                    product: "abc",
                    qty: 10
                },
                {
                    product: "def",
                    qty: 20
                }
            ]
        }
    ])
})
