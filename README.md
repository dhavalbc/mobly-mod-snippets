Modified mobly snippets from following github link.
https://github.com/google/mobly-bundled-snippets

Modified MediaSnippet to control Android 3rd party music players using mobly
(including google play music).

## Usage

1.  Compile and install the bundled snippets

        ./gradlew assembleDebug
        adb install -d -r -g ./build/outputs/apk/debug/mobly-bundled-snippets-debug.apk

1.  Use the Mobly snippet shell to interact with the bundled snippets

        snippet_shell.py com.google.android.mobly.snippet.bundled
        >>> print(s.help())
        Known methods:
        ...
         mediaPause() returns void  // pause Media on default Android music player.
         mediaPlay() returns void   // play Media on default Android music player.
         mediaNext() returns void   // play next song.
         mediaPrev() returns void   // play previous song.
        ...
        
## Develop

  * [Mobly multi-device test framework](http://github.com/google/mobly)
  * [Mobly Snippet Lib](http://github.com/google/mobly-snippet-lib)
