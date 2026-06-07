# Veranderingen aan opzet
## Database 1/10/2025
De werkelijke Database die nu is opgezet bij de klant is een MySQL Databank. Origineel hadden we gedacht te werken met Supabase PostgreSQL. Hierdoor gaan we gebruik maken van Docker om voor iedereen via een `docker-compose.yml` file dezelfde lokale DB op te zetten met een bijhorende seeding data query.

## Authentication 24/11/2025
Het originele plan om de authenticatie via supa-base te maken is veranderd aangezien de databank ook niet meer via supabase zal opgezet worden. Ik heb besloten om de authenticatie volledig intern af te werken. De users en paswoorden worden mee in de lokale databank opgeslagen m.b.v. hashing en salting. Hierbij is gekeken naar guidelines gebruikt in de huidige applicatie development