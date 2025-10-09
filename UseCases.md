# Use Cases - Startspeler HORECA APP

## 1. Account inloggen
### Functionaliteit:
Gebruiker logt in op het systeem via tablet of computer.
### Voorwaarde: 
Gebruiker heeft een geldig account.
### Normaal verloop:
Het systeem toont de loginpagina. De gebruiker voert gebruikersnaam en wachtwoord in. Het systeem valideert de gegevens. De gebruiker krijgt toegang tot de extra functies in de applicatie. Het systeem toont het dashboard van de gebruiker.
### Uitzonderingen:
- Onjuiste inloggegevens: foutmelding en mogelijkheid tot opnieuw proberen.
### Definition of Done:
Admin is succesvol ingelogd en heeft toegang tot de functies van een admin.
> **Opmerking:** Alleen de admin met geldige inloggegevens kan inloggen. Klanten hebben geen toegang tot het loginproces.

## 2 Klant registreren
### Functionaliteit:
Een gebruiker registreert een klant in het systeem.
### Voorwaarde:
De klantnaam en/of e-mailadres is nog niet geregistreerd in het systeem.
### Normaal verloop:
De gebruiker opent het klant toevoegen scherm en vult de naam van de klant in. Optioneel kan een e-mailadres worden ingevoerd dat aan de klant wordt gekoppeld. Het systeem controleert op bestaande of vergelijkbare klantnamen en of het e-mailadres al is geregistreerd. Indien er geen duplicaten zijn, wordt de klant geregistreerd.
### Uitzonderingen:
- Naam lijkt op bestaande klant: systeem toont melding en vraagt bevestiging om het account toch aan te maken.
- Naam van klant is al geregistreerd en toont een melding en vraagt om opnieuw te proberen met een andere naam.
- E-mailadres is al geregistreerd: het systeem toont een melding en vraagt om opnieuw te proberen met een ander e-mailadres (of zonder).
### Definition of Done:
Nieuwe klant is toegevoegd en zichtbaar in het systeem.

## 3. Klanten beheren
### Functionaliteit:
Admin kan klantgegevens aanpassen, verwijderen of groepen toewijzen.
### Voorwaarde:
Admin is ingelogd.
### Normaal verloop:
Het systeem toont het klantenoverzicht scherm. De admin zoekt de klant op via filters en past de gegevens aan of wijst een groep toe. Na bevestiging van de wijzigingen worden deze door het systeem opgeslagen.
### Uitzonderingen:
- Klant niet gevonden: melding op scherm (Geen klant met deze naam/e-mailadres gevonden in het systeem).
- Naam lijkt op bestaande klant: systeem toont melding en vraagt bevestiging om de naam toch te wijzigen.
- Naam van klant is al geregistreerd en toont een melding en vraagt om opnieuw te proberen met een andere naam.
- E-mailadres is al geregistreerd: het systeem toont een melding en vraagt om opnieuw te proberen met een ander e-mailadres (of zonder).
### Definition of Done:
Gebruikersgegevens zijn correct aangepast.

## 4. Producten beheren
### Functionaliteit:
Admin kan producten toevoegen, wijzigen of verwijderen.
### Voorwaarde:
Admin is ingelogd.
### Normaal verloop:
De admin opent het productbeheer en voegt een product toe of past een bestaand product aan. Het systeem slaat de wijzigingen op.
### Uitzonderingen:
- Product bestaat al: foutmelding.
### Definition of Done:
Producten zijn correct toegevoegd of aangepast.

## 5. Stock beheren
### Functionaliteit:
Admin kan voorraad van producten beheren.
### Voorwaarde:
Admin is ingelogd.
### Normaal verloop:
De admin bekijkt de voorraadlijst en past de voorraad aan. Het systeem registreert de wijziging en de datum.
### Uitzonderingen:
- Product niet gevonden: foutmelding.
### Definition of Done:
Voorraad is correct aangepast en up-to-date.

## 6. Groepen beheren
### Functionaliteit:
Admin kan klantgroepen en bijbehorende kortingen beheren.
### Voorwaarde:
Admin is ingelogd.
### Normaal verloop:
De admin opend de pagina met groepoverzicht. De admin voegt een groep toe of past de groepgevens (groepnaam, korting) aan. Het systeem slaat de wijzigingen op.
### Uitzonderingen:
- Groep bestaat al: foutmelding.
- korting is geen geldige waarde: foutmelding.
### Definition of Done:
Groepen en kortingen zijn correct toegevoegd/aangepast.

## 7. Tafels beheren
### Functionaliteit:
Admin kan tafels toevoegen en status wijzigen.
### Voorwaarde:
Admin is ingelogd.
### Normaal verloop:
De admin voegt een tafel toe of wijzigt de status. Het systeem slaat de wijzigingen op.
### Uitzonderingen:
- Tafel bestaat al: foutmelding.
### Definition of Done:
Tafels zijn correct toegevoegd en status is actueel.

## 8. Bestellingen beheren
### Functionaliteit:
Een admin kan bestellingen bekijken, aanpassen en status wijzigen.
### Voorwaarde:
Admin is ingelogd.
### Normaal verloop:
Het systeem opent een overzichtspagina van alle bestellingen. De admin kan filteren tussen de bestellingen. De admin past een bestelling aan of wijzigt de status. Het systeem slaat de wijzigingen op.
### Uitzonderingen:
- Bestelling niet gevonden: foutmelding.
### Definition of Done:
Bestellingen zijn correct verwerkt en status is actueel.

## 9. Bestellingen plaatsen
### Functionaliteit:
gebruiker plaatst een bestelling via de applicatie.
### Voorwaarde:
gebruiker heeft een geldige klantnaam en tafelnummer geselecteerd.
### Normaal verloop:
De gebruiker selecteert de klant, de tafel en de gewenste producten. De bestelling wordt toegevoegd. Het systeem toont een bevestiging en voegt de bestelling toe aan het overzicht.
### Uitzonderingen:
- Product niet beschikbaar: systeem toont melding.
- tafelnummer niet geldig: systeem toont een melding.
### Definition of Done:
Bestelling is succesvol geplaatst en zichtbaar bij de bar.

## 10. E-mail koppelen
### Functionaliteit:
Klant kan e-mailadres koppelen aan profiel.
### Voorwaarde:
Klantnaam is geregistreerd.
### Normaal verloop:
Het systeem toont een pagina on e-mailadres te linken aan eenklant. De gebruiker geeft een klantnaam en e-mailadres in. Het systeem koppelt dan het e-mailadres de klant.
> **Opmerking:** Eventueel maken met bevestigings code
### Uitzonderingen:
- E-mailadres bestaat al: toont melding en vraagt voor ander e-mailadres.
### Definition of Done:
E-mail is correct gekoppeld aan klantprofiel.

## 11. Bestellingen Afrekenen
### Functionaliteit:
Admin kan de bestellingen van een klant afrekenen
### Voorwaarde:
Klantnaam is geregistreerd en admin is ingelogd.
### Normaal verloop:
Het systeem toont een overzichtspagina van alle bestellingen van de klant. Het systeem berekend de totaalprijs. De Admin verekend met extern systeem. De Admin duid de bestellingen aan als betaald.
### Definition of Done:
Bestellingen zijn afgerond en aangeduid als betaald in het systeem.