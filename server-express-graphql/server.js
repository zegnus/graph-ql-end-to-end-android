const express = require('express')
const graphql = require('express-graphql')
const server = express();

const { buildSchema } = require('graphql');
const lodash = require('lodash')

// dummy data

var books = [
    { name: "Book 1", genre: 'Fantasy', id: '1' },
    { name: "Book 2", genre: 'Fantasy', id: '2' },
    { name: "Book 3", genre: 'Sci-Fi', id: '3' },
]

const schema = buildSchema(`
    type Query {
        book(id: String!): Book
    }

    type Book {
        id: String
        name: String
        genre: String
    }
`)

const root = {
    book: function (args) {
        return lodash.find(books, {id: args.id})
    }
}

// http://localhost:4000/graphql
server.use('/graphql', graphql({
    schema: schema,
    graphiql: true,
    rootValue: root
}))

server.listen(4000, () => {
    console.log('listening requests on 4000')
})
