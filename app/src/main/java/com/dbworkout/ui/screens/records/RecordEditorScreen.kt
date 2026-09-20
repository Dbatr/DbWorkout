package com.dbworkout.ui.screens.records

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dbworkout.R
import com.dbworkout.ui.components.DatePickerModal
import com.dbworkout.ui.components.LoadingPane
import com.dbworkout.ui.util.formatRussianDate
import com.dbworkout.viewmodel.RecordEditorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordEditorScreen(
    viewModel: RecordEditorViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerModal(
            initialDate = state.date,
            onDismiss = { showDatePicker = false },
            onDateSelected = viewModel::setDate,
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (state.recordId == null) R.string.new_record else R.string.edit_record),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { scope.launch { if (viewModel.save().isSuccess) onSaved() } },
                        enabled = !state.isLoading && !state.isSaving,
                    ) { Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.save)) }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingPane(Modifier.padding(padding))
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.isSaving) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Text(
                        state.exerciseName,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                OutlinedTextField(
                    value = state.weight,
                    onValueChange = viewModel::setWeight,
                    label = { Text(stringResource(R.string.weight_kg)) },
                    placeholder = { Text(stringResource(R.string.record_weight_hint)) },
                    singleLine = true,
                    isError = state.weightError || state.duplicateDateError || state.saveError,
                    supportingText = when {
                        state.weightError -> ({ Text(stringResource(R.string.error_record_weight)) })
                        state.duplicateDateError -> ({ Text(stringResource(R.string.error_duplicate_record)) })
                        state.saveError -> ({ Text(stringResource(R.string.error_save_record)) })
                        else -> null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("record_weight"),
                )
                Text(stringResource(R.string.date), style = MaterialTheme.typography.labelLarge)
                OutlinedCard(onClick = { showDatePicker = true }, shape = RoundedCornerShape(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(state.date.formatRussianDate(), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = stringResource(R.string.date_field_description))
                    }
                }
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::setNotes,
                    label = { Text(stringResource(R.string.record_notes)) },
                    placeholder = { Text(stringResource(R.string.record_notes_hint)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("record_notes"),
                )
                Spacer(Modifier.fillMaxWidth())
            }
        }
    }
}
