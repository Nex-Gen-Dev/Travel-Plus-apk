package com.example.data.models

data class VisaInfo(
    val destinationCountry: String,
    val passportNationality: String,
    val status: String, // "Visa-Free", "eVisa / ETA Required", "Visa on Arrival", "Embassy Visa Required"
    val maxStay: String,
    val notes: String,
    val officialPortalUrl: String,
    val costEstimate: String
)

data class TravelAdvisoryInfo(
    val country: String,
    val level: Int, // 1: Exercise Normal Precautions, 2: Exercise Increased Caution, 3: Reconsider Travel, 4: Do Not Travel
    val title: String,
    val summary: String,
    val lastUpdated: String,
    val emergencyHotline: String,
    val usEmbassyAddress: String,
    val ukEmbassyAddress: String
)

data class EmergencyInfo(
    val country: String,
    val dialCode: String,
    val police: String,
    val ambulance: String,
    val fire: String,
    val touristPolice: String = "",
    val generalEmergency: String = "112"
)

data class TravelCreditCard(
    val id: String,
    val name: String,
    val issuer: String,
    val annualFee: String,
    val foreignTxFee: String,
    val rewardHighlights: String,
    val loungeAccess: Boolean,
    val applyDeepLink: String,
    val bestForVibe: String,
    val welcomeBonus: String
)

data class CurrencyRate(
    val code: String,
    val name: String,
    val symbol: String,
    val rateAgainstUSD: Double,
    val tipCulture: String
)

object ReferenceDataStore {
    val currencies = listOf(
        CurrencyRate("USD", "US Dollar", "$", 1.0, "18% - 22% customary in restaurants"),
        CurrencyRate("EUR", "Euro", "€", 0.92, "Round up or 5% - 10% for good service"),
        CurrencyRate("JPY", "Japanese Yen", "¥", 154.5, "No tipping (can be considered impolite)"),
        CurrencyRate("GBP", "British Pound", "£", 0.79, "10% - 12.5% (often included in bill)"),
        CurrencyRate("CAD", "Canadian Dollar", "C$", 1.38, "15% - 20% standard"),
        CurrencyRate("AUD", "Australian Dollar", "A$", 1.52, "Not expected; 5-10% for exceptional dining"),
        CurrencyRate("CHF", "Swiss Franc", "CHF", 0.89, "Included in service; round up small amounts"),
        CurrencyRate("THB", "Thai Baht", "฿", 36.2, "20-50 THB per bill or 10% in upscale spots"),
        CurrencyRate("MXN", "Mexican Peso", "Mex$", 19.5, "10% - 15% standard (propina)"),
        CurrencyRate("SGD", "Singapore Dollar", "S$", 1.34, "10% service charge usually added to bill"),
        CurrencyRate("INR", "Indian Rupee", "₹", 83.9, "5% - 10% in dine-in restaurants"),
        CurrencyRate("AED", "UAE Dirham", "AED", 3.67, "10% - 15% customary in Dubai/Abu Dhabi"),
        CurrencyRate("BRL", "Brazilian Real", "R$", 5.45, "10% service charge 'serviço' included"),
        CurrencyRate("NZD", "New Zealand Dollar", "NZ$", 1.66, "Not expected; discretionary for top service"),
        CurrencyRate("KRW", "South Korean Won", "₩", 1380.0, "No tipping culture")
    )

    val emergencyDirectory = listOf(
        EmergencyInfo("Japan", "+81", "110", "119", "119", "03-3501-0110", "110 / 119"),
        EmergencyInfo("France", "+33", "17", "15", "18", "", "112"),
        EmergencyInfo("United Kingdom", "+44", "999", "999", "999", "", "999 / 112"),
        EmergencyInfo("Italy", "+39", "113", "118", "115", "", "112"),
        EmergencyInfo("Thailand", "+66", "191", "1669", "199", "1155 (Tourist Police English)", "112"),
        EmergencyInfo("Mexico", "+52", "911", "911", "911", "078 (Green Angels Roadside)", "911"),
        EmergencyInfo("Spain", "+34", "091", "061", "080", "902-102-112", "112"),
        EmergencyInfo("Germany", "+49", "110", "112", "112", "", "112"),
        EmergencyInfo("Australia", "+61", "000", "000", "000", "", "000 / 112"),
        EmergencyInfo("United States", "+1", "911", "911", "911", "", "911"),
        EmergencyInfo("Canada", "+1", "911", "911", "911", "", "911"),
        EmergencyInfo("Switzerland", "+41", "117", "144", "118", "", "112"),
        EmergencyInfo("Singapore", "+65", "999", "995", "995", "", "999 / 995"),
        EmergencyInfo("South Korea", "+82", "112", "119", "119", "1330 (Travel Helpline)", "112"),
        EmergencyInfo("Greece", "+30", "100", "166", "199", "171 (Tourist Police)", "112"),
        EmergencyInfo("Iceland", "+354", "112", "112", "112", "", "112"),
        EmergencyInfo("Costa Rica", "+506", "911", "911", "911", "800-8000-645", "911"),
        EmergencyInfo("UAE", "+971", "999", "998", "997", "800-4888", "999"),
        EmergencyInfo("Indonesia", "+62", "110", "118", "113", "110", "112"),
        EmergencyInfo("Portugal", "+351", "112", "112", "112", "", "112")
    )

    val travelAdvisories = listOf(
        TravelAdvisoryInfo(
            country = "Japan",
            level = 1,
            title = "Level 1: Exercise Normal Precautions",
            summary = "Japan has very low crime rates. Be mindful of occasional natural hazards (typhoons, earthquakes).",
            lastUpdated = "Updated 2 weeks ago",
            emergencyHotline = "+81 3-3224-5000",
            usEmbassyAddress = "1-10-5 Akasaka, Minato-ku, Tokyo 107-8420",
            ukEmbassyAddress = "No 1 Ichiban-cho, Chiyoda-ku, Tokyo 102-8381"
        ),
        TravelAdvisoryInfo(
            country = "France",
            level = 2,
            title = "Level 2: Exercise Increased Caution",
            summary = "Exercise increased caution in major metropolitan areas due to potential civil unrest, strikes, and pickpocketing in tourist hotspots.",
            lastUpdated = "Updated this month",
            emergencyHotline = "+33 1 43 12 22 22",
            usEmbassyAddress = "2 Avenue Gabriel, 75008 Paris",
            ukEmbassyAddress = "35 Rue du Faubourg Saint-Honoré, 75008 Paris"
        ),
        TravelAdvisoryInfo(
            country = "Italy",
            level = 2,
            title = "Level 2: Exercise Increased Caution",
            summary = "Exercise caution in crowded tourist sites (Colosseum, Duomo) due to active petty theft/bag snatching rings.",
            lastUpdated = "Updated this month",
            emergencyHotline = "+39 06 46741",
            usEmbassyAddress = "Via Vittorio Veneto, 121, 00187 Roma RM",
            ukEmbassyAddress = "Via XX Settembre, 80a, 00187 Roma RM"
        ),
        TravelAdvisoryInfo(
            country = "United Kingdom",
            level = 1,
            title = "Level 1: Exercise Normal Precautions",
            summary = "Safe destination with high security standards. Mind transit strikes and busy subway stations.",
            lastUpdated = "Updated recently",
            emergencyHotline = "+44 20 7499 9000",
            usEmbassyAddress = "33 Nine Elms Ln, Nine Elms, London SW11 7US",
            ukEmbassyAddress = "Local Foreign Office, Whitehall, London"
        ),
        TravelAdvisoryInfo(
            country = "Thailand",
            level = 1,
            title = "Level 1: Exercise Normal Precautions",
            summary = "Generally safe for international travelers. Avoid unmetered tuk-tuks, beware of jet ski scams, and respect local laws.",
            lastUpdated = "Updated recently",
            emergencyHotline = "+66 2 205 4000",
            usEmbassyAddress = "95 Wireless Rd, Lumphini, Pathum Wan, Bangkok 10330",
            ukEmbassyAddress = "14 Wireless Rd, Lumphini, Pathum Wan, Bangkok 10330"
        ),
        TravelAdvisoryInfo(
            country = "Mexico",
            level = 2,
            title = "Level 2: Exercise Increased Caution",
            summary = "Major resort areas (Cancun, Riviera Maya, Los Cabos, Mexico City Roma/Condesa) are generally safe with regular police presence.",
            lastUpdated = "Updated this month",
            emergencyHotline = "+52 55 8526 2561",
            usEmbassyAddress = "Paseo de la Reforma 305, Cuauhtémoc, Mexico City",
            ukEmbassyAddress = "Rio Lerma 71, Col. Cuauhtémoc, 06500 Mexico City"
        ),
        TravelAdvisoryInfo(
            country = "Spain",
            level = 2,
            title = "Level 2: Exercise Increased Caution",
            summary = "Exercise caution in Barcelona (Las Ramblas, Gothic Quarter) and Madrid plazas regarding pickpocketing.",
            lastUpdated = "Updated recently",
            emergencyHotline = "+34 91 587 2200",
            usEmbassyAddress = "Calle de Serrano, 75, 28006 Madrid",
            ukEmbassyAddress = "Torre Espacio, Paseo de la Castellana 259D, 28046 Madrid"
        ),
        TravelAdvisoryInfo(
            country = "Switzerland",
            level = 1,
            title = "Level 1: Exercise Normal Precautions",
            summary = "One of the safest countries in the world. Follow mountain alpine safety guides when trekking.",
            lastUpdated = "Updated recently",
            emergencyHotline = "+41 31 357 70 11",
            usEmbassyAddress = "Sulgeneckstrasse 19, 3007 Bern",
            ukEmbassyAddress = "Thunstrasse 50, 3005 Bern"
        )
    )

    val creditCards = listOf(
        TravelCreditCard(
            id = "chase_sapphire_reserve",
            name = "Chase Sapphire Reserve®",
            issuer = "Chase",
            annualFee = "$550",
            foreignTxFee = "$0 (No Foreign Transaction Fees)",
            rewardHighlights = "3x points on dining & travel, $300 annual travel credit, Priority Pass lounge access, primary rental car insurance.",
            loungeAccess = true,
            applyDeepLink = "https://creditcards.chase.com/rewards-credit-cards/sapphire/reserve",
            bestForVibe = "Luxury & Frequent Flyers",
            welcomeBonus = "60,000 Bonus Points after spending $4,000 in first 3 months"
        ),
        TravelCreditCard(
            id = "capital_one_venture_x",
            name = "Capital One Venture X",
            issuer = "Capital One",
            annualFee = "$395",
            foreignTxFee = "$0 (No Foreign Transaction Fees)",
            rewardHighlights = "2x miles on all purchases, 10x on hotels & cars via portal, $300 annual travel credit, 10,000 anniversary bonus miles.",
            loungeAccess = true,
            applyDeepLink = "https://www.capitalone.com/credit-cards/venture-x/",
            bestForVibe = "Best Value Luxury",
            welcomeBonus = "75,000 Bonus Miles after spending $4,000 in first 3 months"
        ),
        TravelCreditCard(
            id = "chase_sapphire_preferred",
            name = "Chase Sapphire Preferred®",
            issuer = "Chase",
            annualFee = "$95",
            foreignTxFee = "$0 (No Foreign Transaction Fees)",
            rewardHighlights = "3x on dining, 2x on travel, 5x on travel purchased through Chase. $50 annual hotel credit, trip cancellation insurance.",
            loungeAccess = false,
            applyDeepLink = "https://creditcards.chase.com/rewards-credit-cards/sapphire/preferred",
            bestForVibe = "Best Starter Travel Card",
            welcomeBonus = "60,000 Bonus Points after spending $4,000 in first 3 months"
        ),
        TravelCreditCard(
            id = "amex_platinum",
            name = "The Platinum Card® from American Express",
            issuer = "American Express",
            annualFee = "$695",
            foreignTxFee = "$0 (No Foreign Transaction Fees)",
            rewardHighlights = "5x points on flights booked directly or via Amex Travel, Global Lounge Collection (Centurion + Delta), $200 airline fee credit.",
            loungeAccess = true,
            applyDeepLink = "https://www.americanexpress.com/us/credit-cards/card/platinum/",
            bestForVibe = "Ultimate VIP Airport Experience",
            welcomeBonus = "80,000 Membership Rewards® Points after $8,000 spend"
        ),
        TravelCreditCard(
            id = "amex_gold",
            name = "American Express® Gold Card",
            issuer = "American Express",
            annualFee = "$325",
            foreignTxFee = "$0 (No Foreign Transaction Fees)",
            rewardHighlights = "4x points at restaurants worldwide, 4x at US supermarkets, 3x on flights booked directly. Great for international foodies.",
            loungeAccess = false,
            applyDeepLink = "https://www.americanexpress.com/us/credit-cards/card/gold-card/",
            bestForVibe = "Food & Dining Aficionado",
            welcomeBonus = "60,000 Points after spending $6,000 in first 6 months"
        )
    )

    fun getVisaRequirement(destinationCountry: String, passportNationality: String = "United States"): VisaInfo {
        val dest = destinationCountry.trim().lowercase()
        val nat = passportNationality.trim().lowercase()

        return when {
            dest.contains("japan") -> VisaInfo(
                destinationCountry = "Japan",
                passportNationality = "USA / Canada / EU / UK / Australia",
                status = "Visa-Free",
                maxStay = "90 Days (Tourism)",
                notes = "Requires valid passport with at least 6 months validity. Recommended: Visit Japan Web QR for fast customs.",
                officialPortalUrl = "https://www.vjw.digital.go.jp/",
                costEstimate = "Free"
            )
            dest.contains("france") || dest.contains("italy") || dest.contains("spain") || dest.contains("germany") || dest.contains("greece") || dest.contains("switzerland") || dest.contains("portugal") || dest.contains("europe") -> VisaInfo(
                destinationCountry = "Schengen Zone (EU)",
                passportNationality = "USA / Canada / UK / Australia",
                status = "Visa-Free (ETIAS Starting 2025/2026)",
                maxStay = "90 Days within 180-day period",
                notes = "Passport must be valid for at least 3 months beyond intended departure date. ETIAS authorization online application.",
                officialPortalUrl = "https://travel-europe.europa.eu/etias_en",
                costEstimate = "€7 (ETIAS)"
            )
            dest.contains("united kingdom") || dest.contains("london") || dest.contains("england") || dest.contains("scotland") -> VisaInfo(
                destinationCountry = "United Kingdom",
                passportNationality = "USA / Canada / EU / Australia",
                status = "Visa-Free (UK ETA Required)",
                maxStay = "6 Months (Tourism)",
                notes = "Requires UK Electronic Travel Authorisation (ETA) before travel for non-visa nationals.",
                officialPortalUrl = "https://www.gov.uk/electronic-travel-authorisation",
                costEstimate = "£10"
            )
            dest.contains("thailand") || dest.contains("bangkok") || dest.contains("phuket") -> VisaInfo(
                destinationCountry = "Thailand",
                passportNationality = "USA / UK / EU / Canada / Australia",
                status = "Visa-Free (Extended)",
                maxStay = "60 Days (Tourism)",
                notes = "Proof of onward travel ticket and adequate funds (20,000 THB) may be requested at immigration.",
                officialPortalUrl = "https://www.thaievisa.go.th/",
                costEstimate = "Free"
            )
            dest.contains("mexico") || dest.contains("cancun") -> VisaInfo(
                destinationCountry = "Mexico",
                passportNationality = "USA / Canada / EU / UK / Japan",
                status = "Visa-Free (FMM Tourist Card)",
                maxStay = "Up to 180 Days",
                notes = "FMM card provided on flight or electronically at entry. Passport must be valid throughout stay.",
                officialPortalUrl = "https://www.inm.gob.mx/",
                costEstimate = "Free"
            )
            dest.contains("australia") || dest.contains("sydney") -> VisaInfo(
                destinationCountry = "Australia",
                passportNationality = "USA / Canada / EU / UK",
                status = "ETA (Subclass 601) Required",
                maxStay = "Up to 3 Months per visit",
                notes = "Must apply via Australian ETA smartphone app prior to boarding.",
                officialPortalUrl = "https://immi.homeaffairs.gov.au/visas/getting-a-visa/visa-listing/electronic-travel-authority-601",
                costEstimate = "AUD $20"
            )
            dest.contains("indonesia") || dest.contains("bali") -> VisaInfo(
                destinationCountry = "Indonesia (Bali)",
                passportNationality = "USA / UK / EU / Australia",
                status = "Visa on Arrival (e-VoA)",
                maxStay = "30 Days (Extendable by 30 days)",
                notes = "Available online or upon arrival at Ngurah Rai International Airport. Plus Bali Tourist Levy.",
                officialPortalUrl = "https://molina.imigrasi.go.id/",
                costEstimate = "500,000 IDR (~$32 USD)"
            )
            else -> VisaInfo(
                destinationCountry = destinationCountry,
                passportNationality = passportNationality,
                status = "Visa-Free or e-Visa Available",
                maxStay = "30 - 90 Days (Standard Tourism)",
                notes = "Ensure passport has at least 6 months validity from date of arrival with at least 2 blank pages.",
                officialPortalUrl = "https://travel.state.gov/content/travel/en/international-travel.html",
                costEstimate = "Check official consular portal"
            )
        }
    }
}
