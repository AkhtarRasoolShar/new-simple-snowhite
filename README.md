# Snowhite Dry Cleaners & Laundry — Official Android Application

Official Android application for **Snowhite Dry Cleaners & Laundry** (Karachi, Pakistan).

## Overview
Snowhite provides professional dry cleaning, laundry, carpet/curtain care, and household cleaning products across Karachi with convenient doorstep pickup and delivery.

## Key Features
- **Instant Offline-First Catalog**: Dynamic categories, products, and rate lists with permanent disk & in-memory image caching for zero-latency browsing.
- **Snowhite Gold Membership**: Live SW Gold Card verification (`30% OFF` Premium Plus & `20% OFF` Regular/Retail services) and in-app Gold Membership registration.
- **Doorstep Pickup & Order Tracking**: Real-time order status tracking, live rider chat, and post-delivery service ratings.
- **Secure Profile Management**: Two-step OTP verification via WhatsApp or Email for account updates.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (`SnowhiteViewModel`, `SnowhiteRepository`, `StateFlow`)
- **Networking & Caching**: OkHttp, Retrofit, Moshi, and two-layer LruCache + SHA-256 Disk Cache
