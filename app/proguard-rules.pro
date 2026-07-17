# R8-Regeln für den Release-Build (minifyEnabled = true seit v0.9.7).
#
# Bewusst minimal: kotlinx.serialization, Retrofit, OkHttp und Coil bringen ihre eigenen
# Consumer-Regeln mit. Die Wire-Modelle brauchen KEINE Keep-Regeln — kotlinx.serialization
# erzeugt die Serializer zur Compile-Zeit und trägt die JSON-Namen als Strings; dass R8 die
# Kotlin-Feldnamen umbenennt, ist deshalb unschädlich (anders als bei reflexionsbasiertem
# Gson/Moshi, das je DTO eine Keep-Regel bräuchte).
#
# Wer hier etwas ändert: die Release-APK danach auf einem Gerät durchspielen.
# R8-Fehler zeigen sich zur Laufzeit, nicht im Build.

# Tink (via androidx.security-crypto) referenziert Error-Prone-Annotationen, die nur zur
# Compile-Zeit existieren. Nur die Warnung unterdrücken, nichts behalten.
-dontwarn com.google.errorprone.annotations.**
