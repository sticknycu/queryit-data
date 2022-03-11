# queryit-data

In order to have data for my database, I make a request to mega-image api.

Over the exposed api I download JSONs files which contains data about products of mega-image.

In order to have product icons, I download them too.


This microservice serve for another microservice, the core (named queryit-core-backend).

# How this exactly works?

That's a good question. I download the new data and I don't override existance one because I want more and more products.

The existance data is exposed via api and backend-core make a request to this microservice controller to get all the data I got from mega-image.

After that data is manipulated on backend-core microservice and exposed to the frontend.
