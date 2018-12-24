# Basic end-to-end GraphQL implementation with Express and Android-Kotlin with coroutines (part 1)

I decided to investigate what is GraphQL as to gather knowledge on this technology and be able to make informed decisions regarding the future of APIs in my profession. You will find all the code in my github repository https://github.com/zegnus/graph-ql-end-to-end-android

This article I won't actually explain what is it in detail and everything that you can do with it, and I won't compare it either with a traditional REST approach. For that I will link lots of resources at the bottom of this article where you can go and read from people that has done an extendise research on it and created awesome documentation.

I decided to get knowledge about it by aiming to implement an end-to-end solution by myself, that is to create a web server that can accept GraphQL requests, a very simple database and an android client that executes requests to this server and processes the responses.

My aim is to create the `Hello World` of Android and GraphQL.

On my investigation, what I got from GraphQL basically is the following:
- The server side exposes data and all the possive entry points to that data
- The client decides which data to get and how to perform the requests

This is in a bit of a contrast with some approaches that I've seen with REST, where the server side decides in behaf of the client, which data the client needs and how the requests should be. For example, you are going to do one request for your entire screen, and we will tell you which data you need. With GraphQL this is a bit on reverse, the server side says, hey I got this data and you can do these queries, and the client then decides independently which data it needs for its screen and if it wants to do one of more requests. This is specially highlighten when different clients (android, iOS, phone, tablet, Roku, Samsung Tvs) has more or less the same features but actually needs different data.

I have also found out that probably, everything that you can do with GraphQL you can do it also with REST. GraphQL will enforce you a specific methodology and process while with REST is up to you. 

As an example with GraphQL the contract between the client and the server is programatically encoded in a schema file and that's the universal source of truth, you cannot avoid it, you cannot fake it, you cannot do tricks with it, you cannot pretend that you have an schema and then violate it as it won't just compile.

With REST you can work without a schema if you like, you can have a schema if you like with Swagger. You can enforce it, or not. You can lie with it, you can say that something is mandatory when its not, and is up to you to fail some tests if you do so. The schema is not part of the process.

Also, GraphQL is **not** a graph database. There is also some confusion about it. GraphQL is just an entry point for API requests, that's it. You can use a memory database, or SQL or make a REST API to somewhere else. You can use it for a unified entrypoint for all your microservices if you like.

After these main highlights, let's go to the code. I will implement the following:
- A web server with an Express Server (that is JavaScript with node.js) that runs locally in my machine
- An in-memory database where we can do queries to our data
- An Android client that will run **raw** GraphQL queries without any third party libraries, written in Kotlin and co-routines, running on a local emulator with a very basic user interface.

The code will be minimum and non exhaustive at all, this serves as an investigation and is not intended for production purposes at all. My machine has a macOS Mojave 10.14.2.

# Server

We will use `npm` for installing all our dependencies.

```
# Install node.js from https://nodejs.org/en/
mkdir server
cd server
npm init # yes to everything
npm install express
npm install nodemon # will restart the server automatically on any file change
npm install graphql express-graphql
npm install lodash # this will help with our in-memory database queries
```

Once all the dependencies are installed, create a `server.js` file where we will encode our server.

Once done, we'll encode our start server method as a script in the `package.json` file as follows:

```
{
  "name": "server-graphql",
  "version": "1.0.0",
  "description": "",
  "main": "index.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1",
    "start": "nodemon server.js"
  },
  "author": "",
  "license": "ISC",
  "dependencies": {
    "express": "^4.16.4",
    "express-graphql": "^0.7.1",
    "graphql": "^14.0.2",
    "lodash": "^4.17.11",
    "nodemon": "^1.18.6"
  }
}
```

With the command `npm start` you will have the server up and running, but it will only properly work once we define the code for running the server, the schema and the requests that it can process.

## Server (server.js) implementation

Our server to run needs to start an express application, the GraphQL module so that it can understand GraphQL requests and it needs a GraphQL schema definition that that it knows how to process the requests.

I will also activate the `graphiql` feature in the server so that we will have a very nice user interface where we will be able to run requests against the server without implementing any client.

```
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
```

What's happening is that the server is an **express** application that is using any request to the path `/graphql` to be redirected to the `graphql` module, that will use a `schema` defined in `./schema/schema` (yet to be done) and that the feature `graphiql` is enabled.

The server itself will listen in the localhost port `4000` and it will print a log in the command line when its active.

Once the `schema` file is being defined, the server will be able to take requests.

## Schema file implementation

The schema file is a programatic encoding of the types of queries that the server understands and the types of objects that the server can handle. For example, a query will be to retrieve a book with an id, where the book is a defined type in the schema.

For our example, we will encode exactly this, requests to books. For the record, the queries, types (models), databases or anything else doesn't actually need to be in one file; it would be good practice to split it in different files.

### Model definitions

GraphQL with Express defines its own primitives as types. That means that there is no `String` as a primitive, there will be a `GraphQLString`. Custom types are going to part of a generic `GraphQLObjectType` type, that will contain other custom or primitive types.

The basic idea of a model definition is to define the following:
- The internal name of your type, for example `BookType`, so that other types or queries can reference this new one
- The public name of your type, for example `Book`, so that when you do a query to the server, you can use this name in it
- The fields of your type, for example a book will have an id, a name, an author, and so on

For our book model, this is what we are going to define:

```
const BookType = new GraphQLObjectType({
    name: 'Book',
    fields: () => ({
        id: { type: GraphQLString },
        name: { type: GraphQLString },
        genre: { type: GraphQLString }
    })
})
```

To actually make this work we have to load the libraries required for the types that we have just used, so on the top of the file we'll define the following:

```
const graphql = require('graphql')

const { 
    GraphQLObjectType, 
    GraphQLString, 
    GraphQLSchema
} = graphql;
```

That is, from the `GraphQL` module, we are going to use those types.

### Database

For querying data we can do an SQL request, or use MongoDB, or perform any REST request or do whatever you like. For this example and for having something to work as fast as possible I will encode an in-memory database. Also as you can use any database with GraphQL it doesn't really provide lots of value to spend lots of time in going deep onto a proper database implementation.

There are more advance aspects in loading data with GraphQL that are important in GraphQL but this is something that I might cover in other posts.

For now, this is my in-memory database that I've created:

```
const lodash = require('lodash')

var books = [
    { name: "Book 1", genre: 'Fantasy', id: '1' },
    { name: "Book 2", genre: 'Fantasy', id: '2' },
    { name: "Book 3", genre: 'Sci-Fi', id: '3' },
]
```

The module `lodash` will help me to do a query on that array using a field. For example doing `lodash.find(books, '2')` it will return the second row. You can of course implement this by yourself if you like.

### Query definitions

Once we've got our types defined and a database, the last step is to actually create an entry point to our data request. A query will define which types will handle and how to satisfy the user's request. One interesting point is that you can create a circular dependency if you like. For example a book belongs to an author, and at the same time an author has several books. You can create a query that returns all this information by just defining queries to your objects like authors/books, and they linking them in the query definition. GraphQL will orchestrate it all for you.

A query is defined by the following:
- A new query object with the internal name that you like (as this can be used to be referenced by other queries)
- The fields of the query, every field will be composed by
 - Public name
 - Which model type it refers to
 - The arguments of your query that has to match with any field on your model, for example an `id`
- And a `resolver`. This is the core of your request. The resolver is the piece of code that will retrieve the data requested. In this case, as the request is of a book and the request argument is the id, here we will have to implement the database request to retrieve this specific book.

Here's the full implementation:

```
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
```

An example of actual request from a client that matches this definition will look like:
```
book(id: "2") {
 name
 genre    
}
```

That is the first line `book(id: "2")` is what we have defined in the `RootQuery` with the `args` being the `id 2`.

The following fields `name genre` are **not** defined in the query, as those are imported from the `BookType` itself. Those are the fields that the client wants from that book. Notice that there is no code at all for dealing with this kind of field *filtering*; GraphQL does the heavy lifting for you.

The `resolver` uses `lodash` to pull out the `id` out from the predefined `args`.

The last step is to export this query as part of what GraphQL can process, this will contain now just one query but it can contain all the queries that your system can process.

```
module.exports = new GraphQLSchema({
    query: RootQuery
})
```

# Run it all

Now we have everything in place for throwing some queries to our first GraphQL server.

From the command line, let's start our server

```
npm start
```

If everything is correct and in place, you should see something like this:
```
$ npm start

> server-graphql@1.0.0 start /Users/zegnus/Development/graph-ql-end-to-end-android/server-express-graphql
> nodemon server.js

[nodemon] 1.18.6
[nodemon] to restart at any time, enter `rs`
[nodemon] watching: *.*
[nodemon] starting `node server.js`
listening requests on 4000
```

That will be listening on our `localhost`, port `4000` under the `/graphql` address, on any web browser go to `http://localhost:4000/graphql` and you will see the `graphiql` user interface.

Notice that on the right you have documentation. This is awesome because you didn't write any documentation yourself so far for your new endpoint, but GraphQL generates it for you, for free, that is coupled with your actual implementation.

On the left you can create your first query:

```
{
  book(id: "1") {
    id
    name
    genre
  }
}
```

And if you click the play button, then you will execute it and your server will reply on the right. You can change the `id` or the fields that you want to request. It just works, it's awesome.

I encourage you take this basic implementation and create more queries and more types. You will see how easy it is to create complex requests with a minimum set of code.

**Resources**

- Full stack tutorial, I strongly recommend to go through the GraphQL Fundamentals https://www.howtographql.com/
- GitHub experience: https://www.youtube.com/watch?v=broQmxQAMjM
- Akamai GraphQL cache https://www.youtube.com/watch?v=r91_QjaGzWM
- Request batch and cache https://www.youtube.com/watch?v=ld2_AS4l19g
- Interfaces https://medium.com/the-graphqlhub/graphql-tour-interfaces-and-unions-7dd5be35de0d