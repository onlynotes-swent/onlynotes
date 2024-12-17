// package com.github.onlynotesswent.ui.common
//
// import androidx.activity.compose.BackHandler
// import androidx.compose.foundation.layout.Arrangement
// import androidx.compose.foundation.layout.Box
// import androidx.compose.foundation.pager.HorizontalPager
// import androidx.compose.foundation.pager.rememberPagerState
// import androidx.compose.foundation.shape.CircleShape
// import androidx.compose.material3.Scaffold
// import androidx.compose.runtime.collectAsState
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.rememberCoroutineScope
// import androidx.compose.runtime.setValue
// import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
// import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.stringResource
// import androidx.compose.ui.unit.dp
// import com.github.onlynotesswent.MainActivity
// import com.github.onlynotesswent.R
// import com.github.onlynotesswent.model.folder.Folder
// import com.github.onlynotesswent.model.folder.FolderViewModel
// import com.github.onlynotesswent.model.user.UserViewModel
// import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
// import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
// import com.github.onlynotesswent.ui.overview.CreateItemFab
// import kotlinx.coroutines.launch
//
/// **
// * A [NestedScrollConnection] that allows the [HorizontalPager] to be nested in a vertical scroll.
// */
// class PagerNestedScrollConnection : NestedScrollConnection
//
/// **
// * Displays the overview screen which contains a list of publicNotes retrieved from the ViewModel.
// */
// @@ -66,91 +79,118 @@ fun OverviewScreen(
//    userViewModel: UserViewModel,
//    folderViewModel: FolderViewModel
// ) {
// val context = LocalContext.current
//// Handle back press
// BackHandler {
//// Move the app to background
// (context as MainActivity).moveTaskToBack(true)
// }
//
// var expanded by remember { mutableStateOf(false) }
// var showCreateFolderDialog by remember { mutableStateOf(false) }
// var showCreateDialog by remember { mutableStateOf(false) }
//
// val parentFolderId = folderViewModel.parentFolderId.collectAsState()
//
// val pagerState = rememberPagerState(initialPage = 0) { 2 }
// val coroutineScope = rememberCoroutineScope()
// Scaffold(
// modifier = Modifier.testTag("overviewScreen"),
// floatingActionButton = {
// CreateItemFab(
// expandedFab = expanded,
// onExpandedFabChange = { expanded = it },
// showCreateFolderDialog = { showCreateFolderDialog = it },
// showCreateDialog = { showCreateDialog = it },
// pagerState = pagerState)
// },
// bottomBar = {
// BottomNavigationMenu(
// onTabSelect = { route -> navigationActions.navigateTo(route) },
// tabList = LIST_TOP_LEVEL_DESTINATION,
// selectedItem = navigationActions.currentRoute())
// }) { paddingValues ->
// Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
// Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
// HorizontalPager(
// state = pagerState,
// modifier = Modifier.fillMaxSize(),
// pageNestedScrollConnection = PagerNestedScrollConnection()) { page ->
//// Our page content
// if (page == 0) {
// NoteOverviewScreen(
// navigationActions = navigationActions,
// noteViewModel = noteViewModel,
// userViewModel = userViewModel,
// folderViewModel = folderViewModel,
// paddingValues = paddingValues)
//
// if (showCreateDialog) {
// NoteDialog(
// onDismiss = { showCreateDialog = false },
// onConfirm = { newName, visibility ->
// val note =
// Note(
// id = noteViewModel.getNewUid(),
// title = newName,
// date = Timestamp.now(),
// visibility = visibility,
// userId = userViewModel.currentUser.value!!.uid,
// folderId = parentFolderId.value)
// noteViewModel.addNote(note)
// noteViewModel.selectedNote(note)
// showCreateDialog = false
// navigationActions.navigateTo(Screen.EDIT_NOTE)
// },
// action = stringResource(R.string.create))
// }
// } else {
// Text("Page 2")
// if (showCreateDialog) {
// Text("Deck dialog")
// }
// }
// }
// }
//// Logic to show the dialog to create a folder
// if (showCreateFolderDialog) {
// FolderDialog(
// onDismiss = { showCreateFolderDialog = false },
// onConfirm = { newName, visibility ->
// val folderId = folderViewModel.getNewFolderId()
// folderViewModel.addFolder(
// Folder(
// id = folderId,
// name = newName,
// userId = userViewModel.currentUser.value!!.uid,
// parentFolderId = parentFolderId.value,
// visibility = visibility,
// )
// )
// showCreateFolderDialog = false
// navigationActions.navigateTo(
// Screen.FOLDER_CONTENTS.replace(oldValue = "{folderId}", newValue = folderId))
// },
// action = stringResource(R.string.create))
// }
// Row(
// modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
// horizontalArrangement = Arrangement.Center,
// ) {
// repeat(pagerState.pageCount) { iteration ->
// val color =
// if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
// Box(
// modifier =
// Modifier.padding(2.dp)
// .clip(CircleShape)
// .background(color)
// .size(10.dp)
// .clickable {
//// Animate to the selected page when clicked
// coroutineScope.launch { pagerState.animateScrollToPage(iteration) }
// })
// }
// }
// }
// }
// }
//
//
/// **
// * Displays the overview screen which contains a list of publicNotes retrieved from the ViewModel.
// @@ -66,91 +79,118 @@ fun OverviewScreen(
// userViewModel: UserViewModel,
// folderViewModel: FolderViewModel
// ) {
// val context = LocalContext.current
//// Handle back press
// BackHandler {
//// Move the app to background
// (context as MainActivity).moveTaskToBack(true)
// }
//
// var expanded by remember { mutableStateOf(false) }
// var showCreateFolderDialog by remember { mutableStateOf(false) }
// var showCreateDialog by remember { mutableStateOf(false) }
//
// val parentFolderId = folderViewModel.parentFolderId.collectAsState()
//
// val pagerState = rememberPagerState(initialPage = 0) { 2 }
// val coroutineScope = rememberCoroutineScope()
// Scaffold(
// modifier = Modifier.testTag("overviewScreen"),
// floatingActionButton = {
// CreateItemFab(
// expandedFab = expanded,
// onExpandedFabChange = { expanded = it },
// showCreateFolderDialog = { showCreateFolderDialog = it },
// showCreateDialog = { showCreateDialog = it },
// pagerState = pagerState)
// },
// bottomBar = {
// BottomNavigationMenu(
// onTabSelect = { route -> navigationActions.navigateTo(route) },
// tabList = LIST_TOP_LEVEL_DESTINATION,
// selectedItem = navigationActions.currentRoute())
// }) { paddingValues ->
// Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
// Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
// HorizontalPager(
// state = pagerState,
// modifier = Modifier.fillMaxSize(),
// pageNestedScrollConnection = PagerNestedScrollConnection()) { page ->
//// Our page content
// if (page == 0) {
// NoteOverviewScreen(
// navigationActions = navigationActions,
// noteViewModel = noteViewModel,
// userViewModel = userViewModel,
// folderViewModel = folderViewModel,
// paddingValues = paddingValues)
//
// if (showCreateDialog) {
// NoteDialog(
// onDismiss = { showCreateDialog = false },
// onConfirm = { newName, visibility ->
// val note =
// Note(
// id = noteViewModel.getNewUid(),
// title = newName,
// date = Timestamp.now(),
// visibility = visibility,
// userId = userViewModel.currentUser.value!!.uid,
// folderId = parentFolderId.value)
// noteViewModel.addNote(note)
// noteViewModel.selectedNote(note)
// showCreateDialog = false
// navigationActions.navigateTo(Screen.EDIT_NOTE)
// },
// action = stringResource(R.string.create))
// }
// } else {
// Text("Page 2")
// if (showCreateDialog) {
// Text("Deck dialog")
// }
// }
// }
// }
//// Logic to show the dialog to create a folder
// if (showCreateFolderDialog) {
// FolderDialog(
// onDismiss = { showCreateFolderDialog = false },
// onConfirm = { newName, visibility ->
// val folderId = folderViewModel.getNewFolderId()
// folderViewModel.addFolder(
// Folder(
// id = folderId,
// name = newName,
// userId = userViewModel.currentUser.value!!.uid,
// parentFolderId = parentFolderId.value,
// visibility = visibility,
// ))
// showCreateFolderDialog = false
// navigationActions.navigateTo(
// Screen.FOLDER_CONTENTS.replace(oldValue = "{folderId}", newValue = folderId))
// },
// action = stringResource(R.string.create))
// }
// Row(
// modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
// horizontalArrangement = Arrangement.Center,
// ) {
// repeat(pagerState.pageCount) { iteration ->
// val color =
// if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
// Box(
// modifier =
// Modifier.padding(2.dp)
// .clip(CircleShape)
// .background(color)
// .size(10.dp)
// .clickable {
//// Animate to the selected page when clicked
// coroutineScope.launch { pagerState.animateScrollToPage(iteration) }
// })
// }
// }
// }
// }
// }
