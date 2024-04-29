# Crossword filler

One of the first programs I ever wrote - a program to find valid English crossword fills given a grid pattern with optional partial completions.

This project came to mind recently and I looked around on the Wayback Machine.
Turns out I posted the jar on MediaFire and linked to it on an old blog on Jan 2, 2011, and it's [still there](https://www.mediafire.com/file/rwpl49xusm55s2a/WordFillVer2.jar).
I downloaded and opened it on my 2023 MacBook Air, and it ran!
Since it's Java, I'm guessing it runs on other computers, too :)

I'm pretty sure I never put the source code online (looks like my first GH commit was almost a year later in [Dec 2011](https://github.com/khiner/AI-Challenge-2011/commit/9cd23268070eceb859ce34083a6f6ae25e9c7ac7)), but here are my vague recollections from over 13 years ago:
* It uses [beam search](https://en.wikipedia.org/wiki/Beam_search).
* Written in Java. Looking at [the releases](https://www.java.com/releases/), version 6 was the latest at the time.
* UI is Java Swing
* If I remember correctly, it doesn't render _every_ guess on the screen, but rather every N guesses, where N < 10, since the vast majority of time was spend rendering compared to searching.
* For some reason it is very, very yellow (ah simpler times 😅)

![](ScreenRecording.gif)

[Download the jar file](https://github.com/khiner/CrosswordFiller/raw/main/WordFillVer2.jar) and have a blast all weekend long 🤪

To compile,

`$ javac Display.java`

To run,

`$ java Display`
