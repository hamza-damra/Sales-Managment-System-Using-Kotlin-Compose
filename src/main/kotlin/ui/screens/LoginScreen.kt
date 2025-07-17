package ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.auth.AuthService
import data.auth.AuthState
import kotlinx.coroutines.launch
import ui.components.RTLProvider
import ui.theme.AppTheme
import ui.theme.CardStyles
import utils.I18nManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authService: AuthService,
    onLoginSuccess: () -> Unit
) {
    RTLProvider {
        val authState by authService.authState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current
        
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var showSignup by remember { mutableStateOf(false) }

        // Additional fields for signup
        var email by remember { mutableStateOf("") }
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }

        // Validation states
        var usernameError by remember { mutableStateOf<String?>(null) }
        var passwordError by remember { mutableStateOf<String?>(null) }
        var emailError by remember { mutableStateOf<String?>(null) }
        var firstNameError by remember { mutableStateOf<String?>(null) }
        var lastNameError by remember { mutableStateOf<String?>(null) }

        // Validation functions
        fun validateUsername(): Boolean {
            usernameError = when {
                username.isBlank() -> I18nManager.getString("auth.error.username_required")
                else -> null
            }
            return usernameError == null
        }

        fun validatePassword(): Boolean {
            passwordError = when {
                password.isBlank() -> I18nManager.getString("auth.error.password_required")
                else -> null
            }
            return passwordError == null
        }

        fun validateEmail(): Boolean {
            emailError = when {
                email.isBlank() -> I18nManager.getString("auth.error.email_required")
                !email.contains("@") -> I18nManager.getString("auth.error.email_invalid")
                else -> null
            }
            return emailError == null
        }

        fun validateFirstName(): Boolean {
            firstNameError = when {
                firstName.isBlank() -> I18nManager.getString("auth.error.firstName_required")
                else -> null
            }
            return firstNameError == null
        }

        fun validateLastName(): Boolean {
            lastNameError = when {
                lastName.isBlank() -> I18nManager.getString("auth.error.lastName_required")
                else -> null
            }
            return lastNameError == null
        }

        fun validateForm(): Boolean {
            val isUsernameValid = validateUsername()
            val isPasswordValid = validatePassword()

            if (showSignup) {
                val isEmailValid = validateEmail()
                val isFirstNameValid = validateFirstName()
                val isLastNameValid = validateLastName()
                return isUsernameValid && isPasswordValid && isEmailValid && isFirstNameValid && isLastNameValid
            }

            return isUsernameValid && isPasswordValid
        }
        
        // Handle successful authentication
        LaunchedEffect(authState.isAuthenticated) {
            if (authState.isAuthenticated) {
                onLoginSuccess()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.03f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(420.dp)
                    .wrapContentHeight()
                    .padding(20.dp),
                colors = CardStyles.elevatedCardColors(),
                elevation = CardStyles.elevatedCardElevation(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Subtle gradient background within card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.01f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Enhanced Logo and Title Section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Logo with enhanced styling
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                        RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = if (showSignup) I18nManager.getString("auth.signup.title") else I18nManager.getString("auth.login.title"),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = if (showSignup) I18nManager.getString("auth.signup.subtitle") else I18nManager.getString("auth.login.subtitle"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    
                        // Enhanced Signup additional fields
                        if (showSignup) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = {
                                        firstName = it
                                        if (firstNameError != null) validateFirstName()
                                    },
                                    label = {
                                        Text(
                                            I18nManager.getString("auth.firstName"),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = firstNameError != null,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    supportingText = firstNameError?.let { error ->
                                        { Text(error, color = MaterialTheme.colorScheme.error) }
                                    }
                                )

                                OutlinedTextField(
                                    value = lastName,
                                    onValueChange = {
                                        lastName = it
                                        if (lastNameError != null) validateLastName()
                                    },
                                    label = {
                                        Text(
                                            I18nManager.getString("auth.lastName"),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = lastNameError != null,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    supportingText = lastNameError?.let { error ->
                                        { Text(error, color = MaterialTheme.colorScheme.error) }
                                    }
                                )

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it
                                        if (emailError != null) validateEmail()
                                    },
                                    label = {
                                        Text(
                                            I18nManager.getString("auth.email"),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = emailError != null,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    supportingText = emailError?.let { error ->
                                        { Text(error, color = MaterialTheme.colorScheme.error) }
                                    }
                                )
                            }
                        }
                    
                        // Enhanced Username field
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                if (usernameError != null) validateUsername()
                            },
                            label = {
                                Text(
                                    I18nManager.getString("auth.username"),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = usernameError != null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            supportingText = usernameError?.let { error ->
                                { Text(error, color = MaterialTheme.colorScheme.error) }
                            }
                        )

                        // Enhanced Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (passwordError != null) validatePassword()
                            },
                            label = {
                                Text(
                                    I18nManager.getString("auth.password"),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) I18nManager.getString("auth.password.hide") else I18nManager.getString("auth.password.show"),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = passwordError != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (validateForm()) {
                                        coroutineScope.launch {
                                            if (showSignup) {
                                                authService.signup(username, email, password, firstName, lastName)
                                            } else {
                                                authService.login(username, password)
                                            }
                                        }
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            supportingText = passwordError?.let { error ->
                                { Text(error, color = MaterialTheme.colorScheme.error) }
                            }
                        )
                    
                        // Enhanced Error message
                        authState.error?.let { errorMessage ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    
                        // Enhanced Login/Signup button with hover effects
                        EnhancedAuthButton(
                            text = if (showSignup) I18nManager.getString("auth.signup.button") else I18nManager.getString("auth.login.button"),
                            isLoading = authState.isLoading,
                            enabled = !authState.isLoading,
                            onClick = {
                                if (validateForm()) {
                                    coroutineScope.launch {
                                        if (showSignup) {
                                            authService.signup(username, email, password, firstName, lastName)
                                        } else {
                                            authService.login(username, password)
                                        }
                                    }
                                }
                            }
                        )
                    
                        // Enhanced Toggle between login and signup with hover effects
                        EnhancedToggleButton(
                            text = if (showSignup) I18nManager.getString("auth.toggle.login") else I18nManager.getString("auth.toggle.signup"),
                            onClick = {
                                showSignup = !showSignup
                                // Clear form when switching
                                username = ""
                                password = ""
                                email = ""
                                firstName = ""
                                lastName = ""
                                // Clear validation errors
                                usernameError = null
                                passwordError = null
                                emailError = null
                                firstNameError = null
                                lastNameError = null
                            }
                        )

                        // Add bottom spacing to ensure content is not cut off
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// Enhanced Auth Button Component with hover effects
@Composable
private fun EnhancedAuthButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (enabled) {
                    if (isHovered)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    else
                        MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                if (enabled) onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

// Enhanced Toggle Button Component with hover effects
@Composable
private fun EnhancedToggleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isHovered)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                else
                    MaterialTheme.colorScheme.primary
            ),
            interactionSource = interactionSource
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
