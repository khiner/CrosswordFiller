# Crossword filler

One of the first programs I ever wrote - a program to find valid English crossword fills given a grid pattern with optional partial completions.

This project came to mind recently and I looked around on the Wayback Machine.
Turns out I posted the jar on MediaFire and linked to it on an old blog on Jan 2, 2011.
Luckily, there was [_one capture_ of the jar on MediaFire](https://web.archive.org/web/20240123154949/https://download1655.mediafire.com/lbbcl7ez7adgib9-IUIf0OotUQ4kI4yMugl-Z7a2t57Sf6zL1ibXfzI6OpKsYixWErxnSa_xXvfW7VUqBTZMquQnFAxdKdhgMHDW7saQ3hopgoAbW0-Qae5nX9nbIjSjlrs595R8mCsxTh5KCv0ls3-Pizdj5ZDuPJ0Inlg2CUKDws4/rwpl49xusm55s2a/WordFillVer2.jar) from oddly recently (Jan 23 2024).
I downloaded and opened it on my 2023 MacBook Air, and it ran!
Since it's Java, I'm guessing it runs on other computers, too :)

I'm pretty sure I never put the source code online (looks like my first GH commit was almost a year later in [Dec 2011](https://github.com/khiner/AI-Challenge-2011/commit/9cd23268070eceb859ce34083a6f6ae25e9c7ac7)), but here are my vague recollections from over 13 years ago:
* It uses [beam search](https://en.wikipedia.org/wiki/Beam_search).
* Written in Java. Looking at [the releases](https://www.java.com/releases/), version 6 was the latest at the time.
* UI is Java Swing
* If I remember correctly, it doesn't render _every_ guess on the screen, but rather every N guesses, where N < 10, since the vast majority of time was spend rendering compared to searching.
* For some reason it is very, very yellow (ah simpler times 😅)

![](ScreenRecording.gif)

[Download the jar file](https://github.com/khiner/CrosswordFiller/WordFillVer2.jar) and have a blast all weekend long 🤪
