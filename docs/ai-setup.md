# KI-Einrichtung / AI setup

ScatterTo erzeugt die Post-Texte über einen KI-Dienst deiner Wahl. Du brauchst dafür nur einen
API-Token des jeweiligen Anbieters und fügst ihn in der App unter **KI** ein. Der Token bleibt
verschlüsselt auf dem Gerät und wird ausschließlich an den gewählten Anbieter gesendet.

Du kannst die KI in der App auch komplett abschalten. Dann bereitet ScatterTo nur URL, Link-Vorschau
und Netzwerke vor, und du schreibst die Posts selbst.

> [English version below.](#english)

---

## Welchen Dienst nehmen?

| Dienst | Token holen bei | Voreingestelltes Modell | Kurz |
| --- | --- | --- | --- |
| **Mammouth** (Standard) | [mammouth.ai](https://mammouth.ai) | Anbieter-Auswahl (Mistral/Claude/GPT/Gemini) | Ein Token, mehrere Modelle im Hintergrund |
| **Claude** | [console.anthropic.com](https://console.anthropic.com) | `claude-sonnet-4-5` | Eigener Anthropic-Zugang |
| **OpenAI** | [platform.openai.com/api-keys](https://platform.openai.com/api-keys) | `gpt-4.1` | Eigener ChatGPT/OpenAI-Zugang |
| **Gemini** | [aistudio.google.com/apikey](https://aistudio.google.com/apikey) | `gemini-2.5-flash` | Google, mit kostenlosem Kontingent |

Jeder Dienst speichert seinen eigenen Token, du kannst also jederzeit umschalten, ohne etwas neu
einzugeben.

---

## Mammouth (Standard)

1. Konto anlegen auf [mammouth.ai](https://mammouth.ai).
2. Im Dashboard einen **API-Token** erzeugen.
3. Token in der App unter **KI → Mammouth** einfügen.
4. Bei **Modell** einen Anbieter wählen (Mistral, Claude, GPT oder Gemini). ScatterTo löst daraus
   automatisch das aktuelle Flaggschiff-Modell auf. Alternativ eine eigene Modell-ID eintragen.

## Claude (Anthropic)

1. Konto anlegen auf [console.anthropic.com](https://console.anthropic.com).
2. Unter **API keys** einen neuen Key erzeugen (beginnt mit `sk-ant-`).
3. Ein Guthaben ist nötig; unter **Billing** einrichten.
4. Token in der App unter **KI → Claude** einfügen.
5. Modell bei Bedarf anpassen (Standard: `claude-sonnet-4-5`).

## OpenAI (ChatGPT)

1. Konto anlegen auf [platform.openai.com](https://platform.openai.com).
2. Unter [API keys](https://platform.openai.com/api-keys) einen neuen Key erzeugen (beginnt mit `sk-`).
3. Ein Zahlungsmittel/Guthaben ist nötig; unter **Billing** einrichten.
4. Token in der App unter **KI → OpenAI** einfügen.
5. Modell bei Bedarf anpassen (Standard: `gpt-4.1`).

## Gemini (Google)

1. Bei [aistudio.google.com/apikey](https://aistudio.google.com/apikey) mit einem Google-Konto anmelden.
2. **Create API key** wählen und den Key kopiert bereithalten.
3. Token in der App unter **KI → Gemini** einfügen.
4. Modell bei Bedarf anpassen (Standard: `gemini-2.5-flash`).

Google bietet für Gemini ein kostenloses Kontingent, das für gelegentliches Posten meist ausreicht.

---

<a id="english"></a>

## English

ScatterTo writes the post texts through an AI service of your choice. All you need is an API token
from that provider, which you paste into the app under **AI**. The token stays encrypted on the
device and is only ever sent to the provider you selected.

You can also turn the AI off entirely. ScatterTo then only prepares the URL, link preview and
networks, and you write the posts yourself.

### Which service?

| Service | Get a token at | Default model | Note |
| --- | --- | --- | --- |
| **Mammouth** (default) | [mammouth.ai](https://mammouth.ai) | provider choice (Mistral/Claude/GPT/Gemini) | One token, several models behind it |
| **Claude** | [console.anthropic.com](https://console.anthropic.com) | `claude-sonnet-4-5` | Your own Anthropic account |
| **OpenAI** | [platform.openai.com/api-keys](https://platform.openai.com/api-keys) | `gpt-4.1` | Your own ChatGPT/OpenAI account |
| **Gemini** | [aistudio.google.com/apikey](https://aistudio.google.com/apikey) | `gemini-2.5-flash` | Google, with a free tier |

Each service keeps its own token, so you can switch at any time without re-entering anything.

### Mammouth (default)

1. Create an account at [mammouth.ai](https://mammouth.ai).
2. Generate an **API token** in the dashboard.
3. Paste it into the app under **AI → Mammouth**.
4. Pick a provider under **Model** (Mistral, Claude, GPT or Gemini). ScatterTo resolves the current
   flagship model automatically, or enter your own model ID.

### Claude (Anthropic)

1. Create an account at [console.anthropic.com](https://console.anthropic.com).
2. Create a new key under **API keys** (starts with `sk-ant-`).
3. Credits are required; set them up under **Billing**.
4. Paste the token into the app under **AI → Claude**.
5. Adjust the model if you like (default: `claude-sonnet-4-5`).

### OpenAI (ChatGPT)

1. Create an account at [platform.openai.com](https://platform.openai.com).
2. Create a new key under [API keys](https://platform.openai.com/api-keys) (starts with `sk-`).
3. A payment method/credit is required; set it up under **Billing**.
4. Paste the token into the app under **AI → OpenAI**.
5. Adjust the model if you like (default: `gpt-4.1`).

### Gemini (Google)

1. Sign in at [aistudio.google.com/apikey](https://aistudio.google.com/apikey) with a Google account.
2. Choose **Create API key** and copy it.
3. Paste the token into the app under **AI → Gemini**.
4. Adjust the model if you like (default: `gemini-2.5-flash`).

Google offers a free Gemini tier that is usually enough for occasional posting.
