package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object DeepLinkHelper {

    fun openMapsSearch(context: Context, query: String) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val mapUri = Uri.parse("geo:0,0?q=$encodedQuery")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to web maps
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
            openWebUrl(context, webUri.toString())
        }
    }

    fun openUberRide(context: Context, destinationAddress: String, destLat: Double? = null, destLng: Double? = null) {
        val encodedAddr = URLEncoder.encode(destinationAddress, "UTF-8")
        val uriBuilder = StringBuilder("https://m.uber.com/ul/?action=setPickup&pickup=my_location")
        if (destLat != null && destLng != null) {
            uriBuilder.append("&dropoff[latitude]=$destLat&dropoff[longitude]=$destLng")
        }
        uriBuilder.append("&dropoff[formatted_address]=$encodedAddr")
        openWebUrl(context, uriBuilder.toString())
    }

    fun openLyftRide(context: Context, destinationAddress: String, destLat: Double? = null, destLng: Double? = null) {
        val encodedAddr = URLEncoder.encode(destinationAddress, "UTF-8")
        val uriStr = if (destLat != null && destLng != null) {
            "lyft://riderequest?dlat=$destLat&dlng=$destLng"
        } else {
            "https://ride.lyft.com/riderequest?destination=$encodedAddr"
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr))
            context.startActivity(intent)
        } catch (e: Exception) {
            openWebUrl(context, "https://ride.lyft.com/riderequest?destination=$encodedAddr")
        }
    }

    fun openRestaurantBooking(context: Context, restaurantName: String, city: String = "", provider: String = "OpenTable") {
        val query = "$restaurantName $city".trim()
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = when (provider.lowercase()) {
            "resy" -> "https://resy.com/cities?query=$encoded"
            "yelp" -> "https://www.yelp.com/search?find_desc=$encoded"
            else -> "https://www.opentable.com/s?term=$encoded"
        }
        openWebUrl(context, url)
    }

    fun openFlightSearch(context: Context, origin: String = "NYC", destination: String = "Tokyo", departureDate: String = "") {
        val encodedDest = URLEncoder.encode(destination, "UTF-8")
        val encodedOrigin = URLEncoder.encode(origin, "UTF-8")
        val url = "https://www.google.com/travel/flights?q=flights+from+$encodedOrigin+to+$encodedDest"
        openWebUrl(context, url)
    }

    fun openSkyscannerFlight(context: Context, destination: String) {
        val encoded = URLEncoder.encode(destination, "UTF-8")
        openWebUrl(context, "https://www.skyscanner.com/transport/flights/everywhere/$encoded/")
    }

    fun openHotelBooking(context: Context, hotelOrCity: String, provider: String = "Booking.com") {
        val encoded = URLEncoder.encode(hotelOrCity, "UTF-8")
        val url = when (provider.lowercase()) {
            "expedia" -> "https://www.expedia.com/Hotel-Search?destination=$encoded"
            "hotels" -> "https://www.hotels.com/Hotel-Search?destination=$encoded"
            "airbnb" -> "https://www.airbnb.com/s/$encoded/homes"
            else -> "https://www.booking.com/searchresults.html?ss=$encoded"
        }
        openWebUrl(context, url)
    }

    fun openCarRentalSearch(context: Context, location: String) {
        val encoded = URLEncoder.encode(location, "UTF-8")
        openWebUrl(context, "https://www.kayak.com/cars/$encoded")
    }

    fun openShoppingSearch(context: Context, itemQuery: String, retailer: String = "Amazon") {
        val encoded = URLEncoder.encode(itemQuery, "UTF-8")
        val url = when (retailer.lowercase()) {
            "target" -> "https://www.target.com/s?searchTerm=$encoded"
            "best buy", "bestbuy" -> "https://www.bestbuy.com/site/searchpage.jsp?st=$encoded"
            "walmart" -> "https://www.walmart.com/search?q=$encoded"
            "local", "local stores" -> "https://www.google.com/maps/search/$encoded+near+me"
            else -> "https://www.amazon.com/s?k=$encoded&tag=travelplus-20"
        }
        openWebUrl(context, url)
    }

    fun openTransitDirections(context: Context, destination: String) {
        val encoded = URLEncoder.encode(destination, "UTF-8")
        openWebUrl(context, "https://www.google.com/maps/dir/?api=1&destination=$encoded&travelmode=transit")
    }

    fun dialEmergency(context: Context, number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot place call: $number", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareEmergencySOS(context: Context, tripSummary: String, destination: String, hotelAddress: String = "") {
        val text = buildString {
            append("🚨 TRAVEL PLUS EMERGENCY ITINERARY & STATUS 🚨\n")
            append("Destination: $destination\n")
            if (hotelAddress.isNotBlank()) append("Staying at: $hotelAddress\n")
            append("\nTrip Details:\n")
            append(tripSummary)
            append("\n\nSent via Travel Plus AI Emergency Center.")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Emergency SOS"))
    }

    fun openWebUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser for: $url", Toast.LENGTH_SHORT).show()
        }
    }
}
