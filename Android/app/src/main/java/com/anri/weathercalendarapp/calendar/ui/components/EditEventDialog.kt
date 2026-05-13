package com.anri.weathercalendarapp.calendar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.common.view.dialog.AppDialog
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 予定編集ダイアログ
 * AddEventDialogと同じUI構造で、既存の予定データをプリフィルする
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventDialog(
    event: CalendarEvent,
    eventColors: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirm: (String, CreateEventReq) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    // 既存データからプリフィル値を算出
    val initialStartDate = event.start?.let { parseEditDate(it) } ?: LocalDate.now()
    val initialEndDate = event.end?.let { parseEditDate(it) } ?: initialStartDate
    val initialStartTime = if (!event.isAllDayEvent) event.start?.let { parseEditTime(it) } else null
    val initialEndTime = if (!event.isAllDayEvent) event.end?.let { parseEditTime(it) } else null

    // 終日イベントのendは排他的（翌日を指す）ため、表示時は1日引く
    val adjustedEndDate = if (event.isAllDayEvent && initialEndDate.isAfter(initialStartDate)) {
        initialEndDate.minusDays(1)
    } else {
        initialEndDate
    }

    var title by remember { mutableStateOf(event.summary) }
    var isAllDay by remember { mutableStateOf(event.isAllDayEvent) }
    var selectedColorId by remember { mutableStateOf(event.colorId) }
    var showTimeError by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(adjustedEndDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startTimePickerState = rememberTimePickerState(
        initialHour = initialStartTime?.hour ?: 9,
        initialMinute = initialStartTime?.minute ?: 0
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = initialEndTime?.hour ?: 10,
        initialMinute = initialEndTime?.minute ?: 0
    )

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy年M月d日（E）", Locale.JAPANESE)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val focusManager = LocalFocusManager.current
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        focusManager.clearFocus()
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "予定を編集",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Box {
                            if (title.isEmpty()) {
                                Text(
                                    text = "タイトルを追加",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "終日",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it }
                        )
                    }

                    Column {
                        Text(
                            text = "開始日時",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = BorderStroke(
                                    1.dp, MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showStartDatePicker = true }
                            ) {
                                Text(
                                    text = startDate.format(dateFormatter),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                            TimePickerField(
                                label = "開始時刻",
                                state = startTimePickerState,
                                enabled = !isAllDay
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "終了日時",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = BorderStroke(
                                    1.dp, MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showEndDatePicker = true }
                            ) {
                                Text(
                                    text = endDate.format(dateFormatter),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                            TimePickerField(
                                label = "終了時刻",
                                state = endTimePickerState,
                                enabled = !isAllDay
                            )
                        }
                    }

                    if (eventColors.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Palette,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "カラー",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ColorCircle(
                                    color = MaterialTheme.colorScheme.primary,
                                    isSelected = selectedColorId == null,
                                    onClick = { selectedColorId = null }
                                )
                                eventColors.entries
                                    .sortedBy { it.key.toIntOrNull() ?: 0 }
                                    .forEach { (colorId, hexColor) ->
                                        val color = parseHexColor(hexColor)
                                        if (color != null) {
                                            ColorCircle(
                                                color = color,
                                                isSelected = selectedColorId == colorId,
                                                onClick = { selectedColorId = colorId }
                                            )
                                        }
                                    }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDelete != null) {
                        TextButton(onClick = onDelete) {
                            Text(
                                text = "削除",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("キャンセル")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val startTime = if (!isAllDay) {
                                LocalTime.of(startTimePickerState.hour, startTimePickerState.minute)
                            } else null
                            val endTime = if (!isAllDay) {
                                LocalTime.of(endTimePickerState.hour, endTimePickerState.minute)
                            } else null

                            // 時間矛盾チェック
                            if (!isAllDay && startDate == endDate && startTime != null && endTime != null && endTime <= startTime) {
                                showTimeError = true
                                return@TextButton
                            }

                            onConfirm(
                                event.id,
                                CreateEventReq(
                                    summary = title.takeIf { it.isNotBlank() },
                                    isAllDay = isAllDay,
                                    startDate = startDate,
                                    startTime = startTime,
                                    endDate = endDate,
                                    endTime = endTime,
                                    colorId = selectedColorId
                                )
                            )
                        }
                    ) {
                        Text("更新")
                    }
                }
            }
        }
    }

    if (showTimeError) {
        AppDialog(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            body = stringResource(R.string.dialog_time_conflict_body),
            confirmLabel = stringResource(R.string.dialog_time_conflict_confirm),
            onConfirm = { showTimeError = false },
            onDismissRequest = { showTimeError = false }
        )
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.of("UTC"))
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            startDate = selected
                            if (selected.isAfter(endDate)) {
                                endDate = selected
                            }
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("キャンセル")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.of("UTC"))
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            endDate = selected
                            if (selected.isBefore(startDate)) {
                                startDate = selected
                            }
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("キャンセル")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/** 日時文字列から日付部分を抽出 */
private fun parseEditDate(dateTimeStr: String): LocalDate? {
    return try {
        LocalDate.parse(dateTimeStr.substringBefore("T"))
    } catch (_: Exception) {
        null
    }
}

/** 日時文字列から時刻部分を抽出 */
private fun parseEditTime(dateTimeStr: String): LocalTime? {
    if (!dateTimeStr.contains("T")) return null
    return try {
        val timePart = dateTimeStr.substringAfter("T").substringBefore("+").substringBefore("-")
        LocalTime.parse(timePart)
    } catch (_: Exception) {
        null
    }
}

