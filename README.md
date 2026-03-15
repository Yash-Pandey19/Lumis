<div align="center">

# 🔦 Lumis
### Intelligent YouTube Video Recommendation System

*Surfacing the best content through sentiment-aware ranking*

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=java&logoColor=white)
![Stanford NLP](https://img.shields.io/badge/Stanford_CoreNLP-4.5.4-red?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Active-success?style=for-the-badge)
![License](https://img.shields.io/badge/License-Academic-yellow?style=for-the-badge)

<br/>

> **Lumis** fetches YouTube videos, reads what audiences are saying in comments using NLP sentiment analysis, and ranks videos by combining engagement metrics with sentiment scores — going beyond simple popularity metrics.

</div>

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [How It Works](#-how-it-works)
- [System Architecture](#-system-architecture)
- [Data Flow](#-data-flow)
- [Recommendation Formula](#-recommendation-formula)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Setup & Installation](#-setup--installation)
- [Screenshots](#-screenshots)


---

## 🧠 Overview

Most YouTube recommendation systems depend on **watch history** and **view count** alone. Lumis introduces a third dimension — **what the audience is actually saying**.

By running Stanford CoreNLP on user comments, Lumis detects whether audience sentiment is positive, neutral, or negative, and incorporates that signal into a weighted composite score. A video with 10 million views but negative comments will rank lower than a video with fewer views but genuinely positive audience reception.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🎯 **Smart Recommendations** | Combines views, likes, comments and sentiment into one composite score |
| 💬 **NLP Sentiment Analysis** | Stanford CoreNLP analyses audience comments — positive / neutral / negative |
| 🔍 **YouTube API Integration** | Official YouTube Data API v3 — fetches metadata, stats, and comments |
| 🎨 **JavaFX GUI** | Clean search interface with colour-coded ranked results table |
| 🔗 **Clickable Links** | Every result has a direct ▶ Open button to the YouTube video |
| 📊 **CSV Export** | Saves full ranked results and comment data to local files |
| ⚡ **Parallel Processing** | Multi-threaded pipeline — 4 videos processed simultaneously |
| 🔄 **Channel Diversity** | Unique channel filter ensures results from different creators |

---

## ⚙️ How It Works

```mermaid
flowchart TD
    A([👤 User enters search term]) --> B[Construct YouTube API URLs]
    B --> C[Fetch search results\n2 pages × 50 videos]
    C --> D[Filter unique channels]
    D --> E{For each video\nin parallel}
    E --> F[Fetch video stats\nviews · likes · comments]
    E --> G[Fetch up to 50\nuser comments]
    F --> H[Stanford CoreNLP\nSentiment Analysis]
    G --> H
    H --> I[Compute composite\nrecommendation score]
    I --> J[Rank videos\nby score]
    J --> K([📊 Display in GUI + save to CSV])

    style A fill:#2E4A7A,color:#fff
    style K fill:#2E4A7A,color:#fff
    style H fill:#0F6E56,color:#fff
    style I fill:#993C1D,color:#fff
```

---

## 🏗️ System Architecture

```mermaid
graph TB
    subgraph L1["Layer 1 — Presentation"]
        M[myMain.java]
        T[TeroApp.java · JavaFX GUI]
    end

    subgraph L2["Layer 2 — Orchestration"]
        Y[YoutubeApiClient.java]
    end

    subgraph L3["Layer 3 — API Pipeline"]
        A[ApiUrlCreator.java] --> S[SendApiRequest.java]
        S --> J[JSONResponseParser.java]
    end

    subgraph L4["Layer 4 — Processing"]
        F[FilterLists.java]
        NLP[SentimentAnalyser.java]
        R[RecommendationScorer.java]
    end

    subgraph L5["Layer 5 — Data Models & Output"]
        V[VideoData.java]
        C[CommentData.java]
        CH[Channel.java]
        CSV[CSVHandler.java]
    end

    M --> Y
    T --> Y
    Y --> L3
    Y --> L4
    L4 --> L5

    style L1 fill:#EEF2F7
    style L2 fill:#EEEDFE
    style L3 fill:#E1F5EE
    style L4 fill:#FAECE7
    style L5 fill:#F1EFE8
```

---

## 🌊 Data Flow

```mermaid
sequenceDiagram
    participant U as 👤 User
    participant G as 🖥️ GUI
    participant API as 🔴 YouTube API
    participant NLP as 🧠 CoreNLP
    participant OUT as 📁 Output

    U->>G: Enter search term
    G->>API: Search request (2 pages)
    API-->>G: Video list (JSON)
    G->>G: Filter unique channels

    loop For each video (parallel)
        G->>API: Fetch stats + comments
        API-->>G: Video data + comments
        G->>NLP: Analyse comment text
        NLP-->>G: Sentiment score (0.0–2.0)
    end

    G->>G: Compute composite score
    G->>G: Rank by score
    G-->>U: Display ranked results
    G->>OUT: Save CSV files
```

---

## 📐 Recommendation Formula

Each video receives a composite score from **0.0 to 1.0**:

```
Score = (0.35 × normViews) + (0.25 × normLikes) + (0.15 × normComments) + (0.25 × normSentiment)
```

All metrics are normalised to 0–1 before combining so no single factor dominates.

| Signal | Weight | Why |
|---|---|---|
| 👁️ View Count | **35%** | Primary engagement signal |
| 👍 Like Count | **25%** | Direct positive feedback |
| 💬 Comment Count | **15%** | Audience engagement depth |
| 🧠 Sentiment Score | **25%** | Qualitative audience reception |

**Sentiment scoring:**
```
0.0 ──────── 1.0 ──────── 2.0
Negative    Neutral    Positive
```

---

## 🛠️ Tech Stack

<div align="center">

| Technology | Version | Purpose |
|---|---|---|
| ☕ Java | 21 | Core language |
| 🎨 JavaFX | 21 | Desktop GUI |
| 🔴 YouTube Data API v3 | — | Video data source |
| 🧠 Stanford CoreNLP | 4.5.4 | NLP sentiment analysis |
| 📦 Apache Maven | 3.6+ | Build & dependency management |
| 📄 OpenCSV | 5.8 | CSV file output |
| 📊 Apache POI | 4.1.2 | Excel file support |

</div>

---

## 📁 Project Structure

```
Lumis/
├── src/
│   └── main/java/myproject/icarus/
│       ├── myMain.java               ← Entry point
│       ├── TeroApp.java              ← JavaFX GUI
│       ├── YoutubeApiClient.java     ← Pipeline orchestrator
│       ├── ApiUrlCreator.java        ← YouTube API URL builder
│       ├── SendApiRequest.java       ← HTTP GET handler
│       ├── JSONResponseParser.java   ← JSON → Java objects
│       ├── SentimentAnalyser.java    ← Stanford CoreNLP pipeline
│       ├── RecommendationScorer.java ← Composite scoring + ranking
│       ├── FilterLists.java          ← Unique channel deduplication
│       ├── VideoData.java            ← Video data model
│       ├── CommentData.java          ← Comment data model
│       └── Channel.java             ← Channel data model
├── pom.xml                           ← Maven dependencies
└── README.md
```

---

## 🚀 Setup & Installation

### Prerequisites

- JDK 21 or above
- Apache Maven 3.6+
- YouTube Data API v3 key ([Get one here](https://console.cloud.google.com/))
- IntelliJ IDEA (recommended)

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/Yash-Pandey19/Lumis.git
cd Lumis
```

**2. Add your YouTube API key**

Open `src/main/java/myproject/icarus/YoutubeApiClient.java` and add your key:
```java
static final String API_KEY = "YOUR_API_KEY_HERE";
```

> ⚠️ Never commit your API key. Add it after cloning.

**3. Build with Maven**
```bash
mvn clean install
```

**4. Run**

In IntelliJ IDEA, open `myMain.java` and run with VM option:
```
-Xmx6g
```

### Output

Results are saved automatically to:
```
project-root/
└── {search-term}/
    ├── results_summary.csv   ← ranked videos with all scores
    └── {videoId}.csv         ← comments per video
```

---


