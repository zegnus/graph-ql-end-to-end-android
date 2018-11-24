const express = require('express')
const graphql = require('express-graphql')
const schema = require('./schema/schema')

const server = express();

// http://localhost:4000/graphql
server.use('/graphql', graphql({
    schema: schema,
    graphiql: true
}))

server.listen(4000, () => {
    console.log('listening requests on 4000')
})