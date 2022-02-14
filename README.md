## Run
- `docker-compose up -d`
- Open http://localhost:8080

## Running it not in docker
- Requires node and sbt installed.
- Run `npm install` to download the JavaScript dependencies.
- Run `npx webpack --watch` to continuosly build the JavaScript .
- Run `sbt ~reStart` To run the server and recompile and restart when changes are made.  You will need to update the database configuration in Main.scala.
- When docker is run it will start up a PostgreSQL database and run `sql/create_tables.sql` to create the tables.  If docker is not used that will have to be done manually.

## Design
- The frontend is written in React.  When the page loads it makes a request to the server to get data.  When the user clicks the Add button a POST request is made to the server which returns the id, title, URL and if there is a favicon.  The favicon is accessed by a REST endpoint that takes the id and returns the favicon.
- The backend is written in Scala.  It uses http4s for the server,  Doobie for making queries to the database, and scala-scraper to parse the webpages for a title and favicon.  It is following the Tagless Final pattern.

## Issues
- If you enter an invalid address it will insert it into the database with null values for title and favicon.  It might be better to return an error to the user. Using a React forms framework that handles displaying global and field level errors would be an improvement.
- Using a form would cause hiting enter in the field to submit the form.
- Validation could be done on the URL the user entered.
- The client used to download the favicon doesn't handle redirects well.  For example if it tries to download the favicon https://nbc.com/generetic/favicon.ico it redirects you to https://www.nbc.com/generetic/favicon.ico but the client doesn't handle that well.  A different client or configuration might handle it better.
- Make the data user specific, right now there are no users.  It seems like a security issue that one user could enter a URL that would cause an image to be downloaded and shown to other users.

## Things to do
- It is looking for the favicon by looking for a link tag with a rel attribute that contains the text icon.  For example `<link rel="icon" href="/favicon.ico">`  Some sites seem to just have a favicon.ico relative to the root path.  If no link tag is found it could look to see if a favicon.ico exists.
- Add more Scala unit tests.
- Add some JavaScript unit tests.   
- Use TypeScript
- Don't hardcode config values.