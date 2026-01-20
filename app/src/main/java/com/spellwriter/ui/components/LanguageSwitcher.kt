import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LanguageSwitcher(
    viewModel: LanguageViewModel = viewModel(),
    onLanguageChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    // Initialize with saved language on first composition
    LaunchedEffect(Unit) {
        val savedLanguage = LanguageManager.getCurrentLanguage(context)
        if (currentLanguage != savedLanguage) {
            viewModel.setLanguage(savedLanguage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Language",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LanguageButton(
                language = "en",
                label = "English",
                isSelected = currentLanguage == "en",
                onClick = {
                    LanguageManager.setLocale(context, "en")
                    viewModel.setLanguage("en")
                    onLanguageChanged("en")
                }
            )

            LanguageButton(
                language = "de",
                label = "Deutsch",
                isSelected = currentLanguage == "de",
                onClick = {
                    LanguageManager.setLocale(context, "de")
                    viewModel.setLanguage("de")
                    onLanguageChanged("de")
                }
            )
        }
    }
}

@Composable
fun LanguageButton(
    language: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.width(120.dp)
    ) {
        Text(text = label)
    }
}
