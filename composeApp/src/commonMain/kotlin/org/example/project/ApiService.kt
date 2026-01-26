package org.example.project

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val jsonConfig = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
}

@Serializable
data class UserInfo(
    val id_usuario: Int = 0,
    val nombre_completo: String = "",
    val email: String = "",
    val rol: String = "normal",
    val foto_url: String? = null,
    val peso: String? = null,
    val altura: String? = null,
    val edad: Int? = null,
    val genero: String? = null,
    val fb_url: String? = null,
    val ig_url: String? = null,
    val tk_url: String? = null,
    val wa_num: String? = null,
    val record_peso: Double = 0.0,
    val record_tiempo: Int = 0,
    val dias_suscripcion: Int = 0,
    val premio_constancia: Int = 0,
    val premio_fuerza: Int = 0,
    val premio_determinacion: Int = 0,
    val salon_fama: Int = 0,
    val fecha_premio_constancia: String? = null,
    val fecha_premio_fuerza: String? = null,
    val fecha_premio_determinacion: String? = null,
    val fecha_registro: String? = null
)

@Serializable
data class SubscriptionHistory(
    val id_historial: Int = 0,
    val nombre_usuario: String = "",
    val nombre_admin: String = "",
    val dias_agregados: Int = 0,
    val fecha_accion: String = ""
)

@Serializable
data class Articulo(
    val id_articulo: Int? = null,
    val nombre: String = "",
    val descripcion: String? = null,
    val precio: Double = 0.0,
    val stock: Int = 0,
    val categoria: String = "Articulo",
    val foto_url: String? = null,
    val fotos_adicionales: List<String>? = emptyList()
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val nombre_completo: String,
    val email: String,
    val password: String,
    val rol: String,
    val peso: String,
    val altura: String,
    val edad: Int,
    val genero: String
)

@Serializable
data class LoginResponse(val token: String? = null, val user: UserInfo? = null, val error: String? = null)
@Serializable
data class RegisterResponse(val message: String? = null, val error: String? = null)
@Serializable
data class UpdateUserResponse(val message: String? = null, val foto_url: String? = null, val error: String? = null)

val apiClient = HttpClient {
    install(ContentNegotiation) { json(jsonConfig) }
}

class ApiService {
    private val baseUrl = "http://192.168.100.86/api"

    private fun cleanResponse(text: String): String {
        val startBrace = text.indexOf("{")
        val startBracket = text.indexOf("[")
        if (startBrace == -1 && startBracket == -1) return text
        val start = if (startBrace != -1 && (startBracket == -1 || startBrace < startBracket)) startBrace else startBracket
        val end = if (text[start] == '{') text.lastIndexOf("}") else text.lastIndexOf("]")
        return if (end != -1 && end > start) text.substring(start, end + 1) else text
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            val response = apiClient.post("$baseUrl/users.php?action=login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email.trim(), password.trim()))
            }
            val rawText = response.bodyAsText().trim()
            val text = cleanResponse(rawText)
            if (text.startsWith("{")) jsonConfig.decodeFromString<LoginResponse>(text)
            else LoginResponse(error = "Servidor: $rawText")
        } catch (e: Exception) { LoginResponse(error = "Error: ${e.message}") }
    }

    suspend fun register(nombre: String, email: String, pass: String, rol: String, peso: String, alt: String, edad: Int, gen: String): RegisterResponse {
        return try {
            val response = apiClient.post("$baseUrl/users.php?action=register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(nombre, email, pass, rol, peso, alt, edad, gen))
            }
            val rawText = response.bodyAsText().trim()
            val text = cleanResponse(rawText)
            if (text.startsWith("{")) jsonConfig.decodeFromString<RegisterResponse>(text)
            else RegisterResponse(error = "Servidor: $rawText")
        } catch (e: Exception) { RegisterResponse(error = e.message) }
    }

    suspend fun getUsers(token: String): List<UserInfo> {
        if (token.isEmpty()) return emptyList()
        return try {
            val response = apiClient.get("$baseUrl/users.php") {
                header("Authorization", "Bearer $token")
                accept(ContentType.Application.Json)
            }
            val text = cleanResponse(response.bodyAsText().trim())
            if (text.startsWith("[")) jsonConfig.decodeFromString<List<UserInfo>>(text) else emptyList()
        } catch (_: Exception) { emptyList() }
    }

    suspend fun getSubscriptionHistory(token: String): List<SubscriptionHistory> {
        if (token.isEmpty()) return emptyList()
        return try {
            val response = apiClient.get("$baseUrl/users.php?action=get_history") {
                header("Authorization", "Bearer $token")
            }
            val text = cleanResponse(response.bodyAsText().trim())
            if (text.startsWith("[")) jsonConfig.decodeFromString<List<SubscriptionHistory>>(text) else emptyList()
        } catch (_: Exception) { emptyList() }
    }

    suspend fun decrementSubscriptions(token: String): Boolean = try {
        apiClient.post("$baseUrl/users.php?action=decrement_subs") { header("Authorization", "Bearer $token") }.status == HttpStatusCode.OK
    } catch (_: Exception) { false }

    suspend fun getArticulos(): List<Articulo> = try {
        val text = cleanResponse(apiClient.get("$baseUrl/articulos.php").bodyAsText().trim())
        if (text.startsWith("[")) jsonConfig.decodeFromString<List<Articulo>>(text) else emptyList()
    } catch (_: Exception) { emptyList() }

    suspend fun createArticulo(nombre: String, desc: String, precio: Double, stock: Int, cat: String, fotos: List<ByteArray>): Boolean {
        return try {
            apiClient.submitFormWithBinaryData(url = "$baseUrl/articulos.php", formData = formData {
                append("nombre", nombre); append("descripcion", desc); append("precio", precio.toString())
                append("stock", stock.toString()); append("categoria", cat)
                fotos.forEachIndexed { i, b -> append("fotos_adicionales[]", b, Headers.build { append(HttpHeaders.ContentType, "image/jpeg"); append(HttpHeaders.ContentDisposition, "form-data; name=\"fotos_adicionales[]\"; filename=\"img$i.jpg\"") }) }
            }).status == HttpStatusCode.OK || apiClient.submitFormWithBinaryData(url = "$baseUrl/articulos.php", formData = formData {}).status == HttpStatusCode.Created
        } catch (_: Exception) { false }
    }

    suspend fun updateArticulo(id: Int, nombre: String, desc: String, precio: Double, stock: Int, cat: String, nuevas: List<ByteArray>, existen: List<String>, principal: String?): Boolean {
        return try {
            apiClient.submitFormWithBinaryData(url = "$baseUrl/articulos.php?id=$id&action=update", formData = formData {
                append("nombre", nombre); append("descripcion", desc); append("precio", precio.toString())
                append("stock", stock.toString()); append("categoria", cat)
                append("fotos_existentes", jsonConfig.encodeToString<List<String>>(existen))
                if (principal != null) append("foto_url_actual", principal)
                nuevas.forEachIndexed { i, b -> append("fotos_adicionales[]", b, Headers.build { append(HttpHeaders.ContentType, "image/jpeg"); append(HttpHeaders.ContentDisposition, "form-data; name=\"fotos_adicionales[]\"; filename=\"upd$i.jpg\"") }) }
            }).status == HttpStatusCode.OK
        } catch (_: Exception) { false }
    }

    suspend fun deleteArticulo(id: Int): Boolean = try { apiClient.delete("$baseUrl/articulos.php?id=$id").status == HttpStatusCode.OK } catch (_: Exception) { false }

    suspend fun updateUser(token: String, user: UserInfo, imageBytes: ByteArray?): String? {
        return try {
            val response = apiClient.submitFormWithBinaryData(url = "$baseUrl/users.php?id=${user.id_usuario}&action=update", formData = formData {
                append("nombre_completo", user.nombre_completo); append("fb_url", user.fb_url ?: ""); append("ig_url", user.ig_url ?: "")
                append("tk_url", user.tk_url ?: ""); append("wa_num", user.wa_num ?: ""); append("foto_url_actual", user.foto_url ?: "")
                if (imageBytes != null && imageBytes.isNotEmpty()) append("foto", imageBytes, Headers.build { append(HttpHeaders.ContentType, "image/jpeg"); append(HttpHeaders.ContentDisposition, "form-data; name=\"foto\"; filename=\"profile.jpg\"") })
            }) { header("Authorization", "Bearer $token") }
            val text = cleanResponse(response.bodyAsText().trim())
            if (text.startsWith("{")) jsonConfig.decodeFromString<UpdateUserResponse>(text).foto_url else null
        } catch (_: Exception) { null }
    }

    suspend fun updateAdminUser(token: String, user: UserInfo, adminName: String, newPassword: String? = null): Boolean {
        return try {
            apiClient.submitForm(url = "$baseUrl/users.php?id=${user.id_usuario}&action=update", formParameters = parameters {
                append("nombre_completo", user.nombre_completo); append("peso", user.peso ?: ""); append("altura", user.altura ?: "")
                append("genero", user.genero ?: "Masculino")
                append("record_peso", user.record_peso.toString()); append("record_tiempo", user.record_tiempo.toString())
                append("premio_constancia", user.premio_constancia.toString()); append("premio_fuerza", user.premio_fuerza.toString())
                append("premio_determinacion", user.premio_determinacion.toString()); append("dias_suscripcion", user.dias_suscripcion.toString())
                append("admin_name", adminName)
                if (!newPassword.isNullOrBlank()) append("password", newPassword)
            }) { header("Authorization", "Bearer $token") }.status == HttpStatusCode.OK
        } catch (_: Exception) { false }
    }

    suspend fun deleteUser(token: String, id: Int): Boolean = try { apiClient.delete("$baseUrl/users.php?id=$id") { header("Authorization", "Bearer $token") }.status == HttpStatusCode.OK } catch (_: Exception) { false }
}