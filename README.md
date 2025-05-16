# 📲 AI WhatsApp Assistant

🤖 Progetto realizzato in collaborazione con @marty957
---

## 📌 Descrizione del Progetto

Questo progetto è un assistente intelligente per WhatsApp basato su **ChatGPT**, in grado di rispondere automaticamente ai messaggi degli utenti utilizzando documenti caricati da ogni organizzazione. È stato sviluppato utilizzando:

- **Spring Boot** (per il backend)
- **PostgreSQL** (per il database relazionale)
- **Qdrant** (per il vector DB, utile al Retrieval-Augmented Generation)
- **Twilio Sandbox for WhatsApp** (per gestire i messaggi WhatsApp)
- **OpenAI API** (per generare risposte AI contestuali)
- **Ngrok** (per il tunneling del webhook in locale)

---

## ⚙️ Strumenti Necessari

Assicurati di avere installati questi strumenti, nell’ordine:

1. **Java 17+**
2. **Maven 3.8+**
3. **PostgreSQL**
4. **Docker** (per eseguire Qdrant)
5. **Ngrok**
6. **Twilio Account** (con Sandbox WhatsApp attivata)
7. **OpenAI Account**

---

## 🚀 Come Avviare il Progetto

1. Clona il progetto:
   ```bash
   git clone https://github.com/tuo-utente/yovendo-ai.git
   cd yovendo-ai

2.Configura il file application.properties con le tue chiavi:
  openai.api.key=sk-xxxxx
  
qdrant.url=http://localhost:6333

twilio.account.sid=ACxxxxx

twilio.auth.token=xxxxxxx

twilio.whatsapp.from=whatsapp:+1xxxxxxxxxx

3.Esegui Qdrant in locale (con Docker):
  docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant

4.Esegui l'applicazione Spring Boot:
  ./mvnw spring-boot:run

5.Apri ngrok per il Webhook WhatsApp:
  ngrok http 8080
Copia l’URL fornito (es. https://abc123.ngrok.io) e inseriscilo come webhook nella sandbox Twilio.

---

## 📦 Funzionalità
Aggiunta di organizzazioni con identificativo WhatsApp unico

Upload documenti (PDF o testo)

Embedding automatico dei contenuti con OpenAI text-embedding-ada-002

Indicizzazione in Qdrant

Ricezione e gestione messaggi WhatsApp

RAG: ricerca nei documenti e risposta automatica contestuale

Storico dei messaggi salvati in message_log

---

## 👥 Ruoli nel Sistema
Admin: può caricare documenti e aggiungere organizzazioni

Utente: invia messaggi tramite WhatsApp e riceve risposte AI in base ai documenti caricati

----

## 📌 Note Finali
In modalità gratuita, Twilio ha un limite di 9 messaggi al giorno per numero.

Questo progetto è pensato come MVP ed è facilmente estendibile con un'interfaccia grafica e gestione utenti avanzata.

Per domande o supporto, contattami su GitHub! 😊

---

## 📌 La Parte FrontEnd

https://github.com/marty957/yovendo
