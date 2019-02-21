const graphql = require('graphql')
const lodash = require('lodash')

const { 
    GraphQLObjectType, 
    GraphQLString, 
    GraphQLSchema
} = graphql;

// dummy data

var books = [
    { name: "Book 1", genre: 'Fantasy', id: '1' },
    { name: "Book 2", genre: 'Fantasy', id: '2' },
    { name: "Book 3", genre: 'Sci-Fi', id: '3' },
]

// models

const BookType = new GraphQLObjectType({
    name: 'Book',
    fields: () => ({
        id: { type: GraphQLString },
        name: { type: GraphQLString },
        genre: { type: GraphQLString }
    })
})

// queries

const RootQuery = new GraphQLObjectType({
    name: 'RootQueryType',
    fields: {
        book: {
            type: BookType,
            args: { id: { type: GraphQLString } },
            resolve(parent, args) {
                // find data
                return lodash.find(books, {id: args.id})
            }
        }
    }
})

// export

module.exports = new GraphQLSchema({
    query: RootQuery
})

// mock query
// book(id: "2") {
//  name
//  genre    
//}