# CovidTracker
 
## About

I built this app to learn more about Kotlin and Android app development. 
The app requests an API, that is coming from The COVID Tracking Project: https://covidtracking.com/api. Then, the app extract the data from the API and displays it like we've seen in Binance/Robinhood apps: there is a chart that displays the most recent information and you can choose to expand the timeline (a week/month ago, or all the data), beside that you can scrub your finger to see specific data from the chart.
The design was made to look good in both Light and Dark Modes.

## Built with

* Kotlin
* Android Studio

## Demo

<img src='https://github.com/Enzo-PsK/CovidTracker/blob/main/Screenshots/ezgif.com-gif-maker.gif' title='Demo' height="500" />

## Screenshots (Light/Dark Mode)

<div>
<img src='https://github.com/Enzo-PsK/CovidTracker/blob/main/Screenshots/Screenshot_1632688459.png' title='Demo' height="500" />
<img src='https://github.com/Enzo-PsK/CovidTracker/blob/main/Screenshots/Screenshot_1632692716.png' title='Demo' height="500"/>
</div>


## Libraries used:
- Retrofit: https://github.com/square/retrofit
- GSON: https://github.com/google/gson
- Spark from Robinhood: https://github.com/robinhood/spark
- Ticker from Robinhood: https://github.com/robinhood/ticker
- Nice Spinner: https://github.com/arcadefire/nice-spinner


This app uses data from the [COVID tracking project](https://twitter.com/covid19tracking) as the data source. The app currently displays data for the US, including all the states and territories.
