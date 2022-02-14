FROM node:16.14.0 as node
WORKDIR /app
COPY . /app
RUN npm install
RUN npx webpack

FROM hseeberger/scala-sbt:8u312_1.6.2_2.13.8 as scala
WORKDIR /app
COPY --from=node /app .
CMD ["sbt", "~reStart"]