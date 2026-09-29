package com.saytap.app.data

import com.google.firebase.Firebase
import com.google.firebase.database.database

/**
 * Instancia única de Realtime Database, con la URL explícita.
 * Se centraliza aquí para no repetirla en cada repositorio
 */
internal val sayTapDatabase = Firebase.database("https://saytap-8236c-default-rtdb.firebaseio.com")