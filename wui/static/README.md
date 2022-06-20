# TSL-kasutajaliides

# Kasutamine

Avada fail tsl-ui.html ja selle avamisel peaks tulema selline vaade:
![Näidis](/wui/static/näidis.png)

# TODO

- [X] Refaktoreerida kogu kupatust(html, css, js)(1).
- [ ] Eraldi failidesse jagada html, css ja js. Templated ka eraldi faili.
- [X] Nooled korda teha kõigil.
- [X] Uue testi lisamisel saab seda testi avada ja valida testi tüüp ning tüübi valimisel avab õiged testi valikud.
- [ ] "+" vajutamisel praegu nagu paneks korraks kiiresti kinni ja avab uuesti. See tuleks eemaldada.
- [X] Sõna "kontrollid" ilusti vertikaalselt keskele panna.
- [X] Staatiliste kontrollide juures elemendid, mis võivad nõuda lisainfot tuleks panna listi, kust saaks ise valida, sest hetkel kõik nähtavad ja see tuleb liiga pikk.
- [X] Staatiliste kontrollide juures elementidele, mis ei nõua lisainfot, tuleks panna kolm raadionuppu(jah-peab sisaldama, ei-ei tohi sisaldada, ei kontrollita (vaikeväärtus)).
    * Hetkel on need vertikaalselt paigutatud. Võib ka proovida horisontaalselt paigutada.
- [X] Staatiliste kontrollide juures elemendid, mis võivad nõuda lisainfot tuleks panna ühte ritta ehk on valik ja sellele järgneb tekstilahter. Praegu on need üksteise all.
    * Uurida, mis nendel tekstilahtritel viga, sest tihti peale vajutades need ei avane korralikult.
- [X] "Sisaldab võtmesõna" juures tuleks "ükskõik millise" ja "mitte ühtegi" valikud ära eemaldada.
- [X] "Ükskõik millise" juures lahter ära eemaldada.
    - [ ] "Mitte ühtegi" tuleks kõigil ära eemaldada(NB kontrolli üle).
- [X] "Kasutajasisendid" juures kasutada tekstikasti, sest seal on eraldajaks newline (\n).
- [X] "Sisendfailid" ja "Väljundfailid" puhul tuleks panna nad paremini paari ja lisada võimalus neid juurde lisada. Samuti ka võimalus neid kustutada.
- [X] "Tulemused" juurde lisada valikud (vt docs).
- [ ] Hallid tekstid muuta mustaks, sest praegu halvasti näha.
- [X] Testidele lisada kustutamise võimalus.
- [X] Kui valida testi tüüp ära, siis see lisada nimele juurde. Nt test 1 - Programmi täitmise kontroll. Lisaks anda kasutajale võimalus panna custom nimi.
- [ ] Punktide teema paika panna.
- [X] Sõnumite (before, failed, passed) teema paika panna ehk kuhu, kuidas jne.
- [X] Tutvustav tekst valmis mõelda kasutajale.
