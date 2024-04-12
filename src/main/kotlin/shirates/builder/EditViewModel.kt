package shirates.builder

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import javafx.scene.input.MouseEvent
import shirates.builder.utility.TestElementRelativeUtility
import shirates.builder.utility.undo.Undoable
import shirates.core.driver.*
import shirates.core.driver.TestMode.isiOS
import shirates.core.driver.commandextension.findElements
import shirates.core.driver.commandextension.getCell
import shirates.core.driver.commandextension.getSelector
import shirates.core.driver.commandextension.helper.CellFlowContainer
import shirates.core.logging.TestLog
import shirates.core.utility.element.ElementCategoryExpressionUtility
import shirates.core.utility.exists
import shirates.core.utility.fileExists
import shirates.core.utility.listFiles
import shirates.core.utility.toPath
import java.io.FileNotFoundException
import java.nio.file.Files
import kotlin.io.path.nameWithoutExtension

class EditViewModel(
    val screenBuilderViewModel: ScreenBuilderViewModel
) {
    @Undoable
    lateinit var screenItems: ObservableList<ScreenItem>

    @Undoable
    lateinit var selectorItems: ObservableList<SelectorItem>

    val identityViewModel = CombinationEditorViewModel(keyName = "identity", editViewModel = this)
    val satellitesViewModel = CombinationEditorViewModel(keyName = "satellites", editViewModel = this)
    val headerElementsViewModel = CombinationEditorViewModel(keyName = "header-elements", editViewModel = this)
    val footerElementsViewModel = CombinationEditorViewModel(keyName = "footer-elements", editViewModel = this)
    val overlayElementsViewModel = CombinationEditorViewModel(keyName = "overlay-elements", editViewModel = this)
    val startElementsViewModel = CombinationEditorViewModel(keyName = "start-elements", editViewModel = this)
    val endElementsViewModel = CombinationEditorViewModel(keyName = "end-elements", editViewModel = this)

    val workDirectoryProperty = SimpleStringProperty()
    var workDirectory: String
        get() {
            return workDirectoryProperty.value
        }
        set(value) {
            workDirectoryProperty.set(value)
        }
    val imageRatioProperty = SimpleDoubleProperty()

    val downSelectedProperty = SimpleBooleanProperty()
    val leftSelectedProperty = SimpleBooleanProperty()
    val rightSelectedProperty = SimpleBooleanProperty()
    val upSelectedProperty = SimpleBooleanProperty()

    @Undoable
    var selectedScreenItem: ScreenItem?
        get() {
            return selectedScreenItemProperty.value
        }
        set(value) {
            selectedScreenItemProperty.set(value)
        }
    val selectedScreenItemProperty = SimpleObjectProperty<ScreenItem>()

    @Undoable
    var selectedSelectorItem: SelectorItem?
        get() {
            return selectedSelectorItemProperty.value
        }
        set(value) {
            selectedSelectorItemProperty.set(value)
        }
    val selectedSelectorItemProperty = SimpleObjectProperty<SelectorItem>()

    @Undoable
    var selectedIdentityItem: SelectorItem?
        get() {
            return selectedIdentityItemProperty.value as SelectorItem?
        }
        set(value) {
            selectedIdentityItemProperty.set(value)
        }
    var selectedIdentityItemProperty = SimpleObjectProperty<SelectorItem>()

    @Undoable
    var selectedNickname: String
        get() {
            return selectedNicknameProperty.value
        }
        set(value) {
            selectedNicknameProperty.set(value)
        }
    val selectedNicknameProperty = SimpleStringProperty()

    @Undoable
    var selectedSelectorExpression: String
        get() {
            return selectedSelectorExpressionProperty.value
        }
        set(value) {
            selectedSelectorExpressionProperty.set(value)
        }
    val selectedSelectorExpressionProperty = SimpleStringProperty()


    val widgetProperty = SimpleStringProperty()
    val classOrTypeProperty = SimpleStringProperty()
    val textOrLabelProperty = SimpleStringProperty()
    val idOrNameProperty = SimpleStringProperty()
    val accessProperty = SimpleStringProperty()
    val boundsProperty = SimpleStringProperty()
    val cellProperty = SimpleStringProperty()
    val scrollHostProperty = SimpleStringProperty()

    init {
        downSelectedProperty.set(true)
        setupListeners()
    }

    private fun setupListeners() {
        selectedSelectorItemProperty.addListener { o, old, new ->
            if (new == null) {
                return@addListener
            }
            refreshFields(testElement = new.testElement)
        }
    }

    fun refreshFields(testElement: TestElement) {

        val widget = ElementCategoryExpressionUtility.getCategory(testElement).toString().lowercase()
        widgetProperty.set(widget)
        classOrTypeProperty.set(testElement.classOrType)
        textOrLabelProperty.set(testElement.textOrLabel)
        idOrNameProperty.set(testElement.idOrNameSimple)
        accessProperty.set(testElement.access)
        val bounds = testElement.bounds.toString().split("centerX")[0].trim()
        boundsProperty.set("$bounds width=${testElement.bounds.width}, height=${testElement.bounds.height}")
        val cell = testElement.getCell()
        if (cell.isEmpty) {
            cellProperty.set("")
        } else {
            cellProperty.set(cell.toString())
        }
        val scrollHost = testElement.scrollHost
        if (scrollHost.isEmpty) {
            scrollHostProperty.set("")
        } else {
            scrollHostProperty.set(scrollHost.toString())
        }
    }

    fun clearEditData() {

        selectedSelectorItem = null
        selectedIdentityItem = null
        selectorItems.clear()

        selectedNicknameProperty.set("")
        selectedSelectorExpressionProperty.set("")
        widgetProperty.set("")
        classOrTypeProperty.set("")
        textOrLabelProperty.set("")
        idOrNameProperty.set("")
        accessProperty.set("")
        boundsProperty.set("")
        cellProperty.set("")
        scrollHostProperty.set("")
    }

    fun getOrCreateScreenItem(xmlFile: String): ScreenItem {

        var item = getScreenItem(xmlFile)
        if (item == null) {
            item = ScreenItem(xmlFile = xmlFile)
            screenItems.add(item)
        }
        return item
    }

    fun getScreenItem(xmlFileOrImageFile: String): ScreenItem? {

        val path = xmlFileOrImageFile.toPath()
        val nameWithoutExtension = path.parent.resolve(path.nameWithoutExtension).toString()
        return screenItems.firstOrNull() { it.xmlFile.startsWith("${nameWithoutExtension}.") }
    }

    fun deleteScreenItem(item: ScreenItem) {

        nextScreenItem()
        Files.deleteIfExists(item.imageFile.toPath())
        Files.deleteIfExists(item.xmlFile.toPath())
        screenItems.remove(item)
        if (screenItems.isEmpty()) {
            selectedScreenItem = null
        }
        refreshKeyInfo()
    }

    fun recoverScreenItemFiles() {

        for (screenItem in screenItems) {
            if (screenItem.imageFile.fileExists().not()) {
                val imageFileName = screenItem.imageFile.toPath().fileName
                val imageFilePath = workDirectory.toPath().parent.resolve(imageFileName)
                if (imageFilePath.exists()) {
                    Files.copy(imageFilePath, screenItem.imageFile.toPath())
                }
                val xmlFileName = screenItem.xmlFile.toPath().fileName
                val xmlFilePath = workDirectory.toPath().parent.resolve(xmlFileName)
                if (xmlFilePath.exists()) {
                    Files.copy(xmlFilePath, screenItem.xmlFile.toPath())
                }
            }
        }
    }

    fun getIdPrefix(): String {

        return "${rootElement.packageName}:id/"
    }

    fun getSelectorItemsForWidgets(): List<SelectorItem> {

        val idPrefix = getIdPrefix()
        var widgets = testDrive.findElements(".widget")
        val container = CellFlowContainer()
        container.addAll(widgets)
        widgets = container.getElements()

        val items = widgets.map { it.toSelectorItem(idPrefix = idPrefix, screenItem = selectedScreenItem) }
        return items
    }

    fun copyFilesToWorkDirectory(startLineNo: Int, endLineNo: Int) {

        val files = TestLog.directoryForLog.listFiles().filter { it.nameWithoutExtension.toIntOrNull() != null }
        val targetFiles =
            files.filter { startLineNo < it.nameWithoutExtension.toInt() && it.nameWithoutExtension.toInt() <= endLineNo }
        for (file in targetFiles) {
            Files.copy(file.toPath(), workDirectory.toPath().resolve(file.name))
        }
    }

    fun loadFromDirectory(directory: String = TestLog.directoryForLog.resolve("work").toString()) {

        val dirPath = directory.toPath()
        if (dirPath.exists().not()) {
            throw FileNotFoundException(dirPath.toString())
        }
        var files = dirPath.toFile().listFiles()?.toList() ?: listOf()
        files = files.filter { it.isFile && it.extension == "xml" }
            .sortedBy { it.nameWithoutExtension.toIntOrNull() ?: 9999 }
        if (files.isEmpty()) {
            throw IllegalArgumentException("No data file found in ${directory}.")
        }

        screenBuilderViewModel.settingsViewModel.setupProfile()

        for (f in files) {
            val screenItem = getOrCreateScreenItem(f.toString())
            selectedScreenItem = screenItem
        }
        selectedScreenItem = screenItems.firstOrNull()
        TestElementCache.synced = true
    }

    fun refreshKeyInfo() {
        if (screenItems.count() == 1) {
            screenItems[0].selectorItems.map { it.keyInfo = "" }
            return
        }

        val nicknameCountMap = mutableMapOf<String, Int>()
        for (screenItem in screenItems) {
            for (item in screenItem.selectorItems) {
                if (nicknameCountMap.containsKey(item.nickname).not()) {
                    nicknameCountMap[item.nickname] = 0
                }
                var count = nicknameCountMap[item.nickname]!!
                count++
                nicknameCountMap[item.nickname] = count
            }
        }
        val screenCount = screenItems.count()
        val commonNicknames = nicknameCountMap.entries.filter { it.value == screenCount }.map { it.key }
        val uniqueNicknames = nicknameCountMap.entries.filter { it.value == 1 }.map { it.key }
        for (screenItem in screenItems) {
            for (item in screenItem.selectorItems) {
                item.keyInfo = ""
                if (commonNicknames.contains(item.nickname)) {
                    item.keyInfo = "K"
                } else if (uniqueNicknames.contains(item.nickname)) {
                    val selector = item.testElement.getSelector(item.selectorExpression)
                    if (selector.isContainingRelative.not()) {
                        item.keyInfo = "S"
                    }
                }
            }
        }

    }

    fun nextScreenItem(): ScreenItem? {

        val selectedItem = selectedScreenItem
        val ix = screenItems.indexOf(selectedItem)
        if (ix < 0) return null
        if (ix >= screenItems.count() - 1) return null
        selectedScreenItem = screenItems[ix + 1]
        return selectedScreenItem
    }

    fun previousScreenItem(): ScreenItem? {

        val selectedItem = selectedScreenItem
        val ix = screenItems.indexOf(selectedItem)
        if (ix <= 0) return null
        selectedScreenItem = screenItems[ix - 1]
        return selectedScreenItem
    }

    fun changeToSelectedScreen() {

        selectorItems.clear()

        val screenItem = selectedScreenItem
        if (screenItem == null) {
            val prefs = screenBuilderViewModel.getPreferences()
            workDirectory = prefs.get("workDirectory", "")
            refresh()
            return
        }
        if (screenItem.rootElement.isEmpty) {
            TestElementCache.loadXml(screenItem.xmlFile.toPath().toFile().readText())
            TestElementCache.synced = true
            screenItem.rootElement = TestElementCache.rootElement
            screenItem.rootTreeItem = TreeItem<TestElement>()

            fun setChildTreeItem(treeItem: TreeItem<TestElement>, testElement: TestElement) {
                treeItem.value = testElement
                for (c in testElement.children) {
                    val ti = TreeItem(c)
                    treeItem.children.add(ti)
                    setChildTreeItem(treeItem = ti, testElement = c)
                }
            }

            val treeItem = TreeItem<TestElement>()
            setChildTreeItem(treeItem = treeItem, testElement = rootElement)
            screenItem.rootTreeItem = treeItem
        }

        TestElementCache.rootElement = screenItem.rootElement
        TestElementCache.synced = true

        if (screenItem.selectorItems.isEmpty()) {
            val items = getSelectorItemsForWidgets()
            screenItem.selectorItems.addAll(items)
        }
        selectorItems.clear()
        selectorItems.addAll(screenItem.selectorItems)
        refresh()
        screenBuilderViewModel.savePreferences()
    }

    fun getTreeItemOf(testElement: TestElement): TreeItem<TestElement>? {

        return selectedScreenItem?.getTreeItemOf(testElement)
    }

    fun getSelectorItemOf(testElement: TestElement): SelectorItem? {

        val e = selectorItems.firstOrNull() { it.testElement == testElement }
        return e
    }

    fun getRatio(): Double {

        var ratio = imageRatioProperty.value
        if (TestMode.isiOS) {
            ratio *= IOS_RATIO
        }
        return ratio
    }

    fun getItemOnImage(e: MouseEvent): SelectorItem? {
        val ratio = getRatio()
        val x = (e.x / ratio).toInt()
        val y = (e.y / ratio).toInt()
        val bounds = Bounds(left = x, top = y, width = 1, height = 1)
        val clickedSelectorItems =
            selectorItems.filter { bounds.isIncludedIn(it.testElement.bounds) }
        val item = clickedSelectorItems.firstOrNull()
        return item
    }

    private var lastClickedElement: TestElement? = null
    private var lastIndex = -1

    fun getElementOnImage(e: MouseEvent): TestElement {

        val elements = getElementsOnImage(e)
        if (elements.isEmpty()) {
            return TestElement.emptyElement
        }
        /**
         * Get element cyclic
         */
        lastIndex++
        if (lastIndex > elements.count() - 1) {
            lastIndex = 0
        }
        val clickedElement = elements.first()
        if (clickedElement != lastClickedElement) {
            lastClickedElement = clickedElement
            lastIndex = 0
        }
        return elements[lastIndex]
    }

    fun getElementsOnImage(e: MouseEvent): List<TestElement> {

        if (selectedScreenItem == null) return listOf()
        val ratio = getRatio()
        val x = (e.x / ratio).toInt()
        val y = (e.y / ratio).toInt()
        val bounds = Bounds(left = x, top = y, width = 1, height = 1)
        var elements = selectedScreenItem!!.rootElement.descendantsAndSelf
            .filter { bounds.isIncludedIn(it.bounds) && it.isEmpty.not() }
            .reversed()
        if (isiOS) {
            elements = elements.filter { it.visible == "true" }
        }
        return elements
    }

    fun refresh() {
        refreshKeyInfo()
        refreshSatelliteCandidates()

        selectedNicknameProperty.set(selectedSelectorItem?.nickname ?: "")
        selectedSelectorExpressionProperty.set(selectedSelectorItem?.selectorExpression ?: "")

        identityViewModel.refresh()
        satellitesViewModel.refresh()
        headerElementsViewModel.refresh()
        footerElementsViewModel.refresh()
        overlayElementsViewModel.refresh()
        startElementsViewModel.refresh()
        endElementsViewModel.refresh()

    }

    fun selectItemOnImage(e: MouseEvent): SelectorItem? {

        val item = getItemOnImage(e) ?: return null
        selectedSelectorItem = item
        return item
    }

    fun setRelativeSelectorFromBaseElement(
        relativeItem: SelectorItem,
        baseItem: SelectorItem
    ): SelectorItem? {

        val relativeExpression =
            TestElementRelativeUtility.getRelativeExpression(baseItem = baseItem, thisItem = relativeItem)
        if (relativeExpression.isBlank()) {
            return null
        }
        relativeItem.nickname =
            TestElementRelativeUtility.getNicknameCandidate(baseItem = baseItem, thisItem = relativeItem)
        relativeItem.selectorExpression = "${baseItem.nickname}$relativeExpression"
        refresh()
        return relativeItem
    }

    val IOS_RATIO = 3.0

    private fun getScrollHostSelectorItemsMap(items: List<SelectorItem>): Map<String, MutableList<SelectorItem>> {

        val scrollHostMap = mutableMapOf<String, MutableList<SelectorItem>>()
        for (item in items) {
            val scrollHost = item.scrollHost
            if (scrollHostMap.containsKey(scrollHost).not()) {
                scrollHostMap[scrollHost] = mutableListOf()
            }
            val list = scrollHostMap[scrollHost]!!
            if (list.any() { it.toString() == item.toString() }.not()) {
                list.add(item)
            }
        }
        return scrollHostMap
    }

    fun getScrollHostSelectorItemsMap(): Map<String, MutableList<SelectorItem>> {
        val scrollableMap = mutableMapOf<String, MutableList<SelectorItem>>()
        for (screenItem in screenItems) {
            mergeSelectorItemsIntoScrollableMap(screenItem, scrollableMap)
        }
        return scrollableMap
    }

    private fun mergeSelectorItemsIntoScrollableMap(
        screenItem: ScreenItem,
        scrollableToSelectorItemsMap: MutableMap<String, MutableList<SelectorItem>>
    ) {
        val map = getScrollHostSelectorItemsMap(items = screenItem.selectorItems)
        for (entry in map) {
            val scrollable = entry.key
            if (scrollableToSelectorItemsMap.containsKey(scrollable).not()) {
                scrollableToSelectorItemsMap[scrollable] = mutableListOf()
            }
            val list = scrollableToSelectorItemsMap[scrollable]!!
            for (item in entry.value) {
                if (list.any() { it.toString() == item.toString() }.not()) {
                    list.add(item)
                }
            }
        }
    }

    fun getSatelliteCandidates(): List<SelectorItem> {

        val candidates = mutableListOf<SelectorItem>()
        for (screenItem in screenItems) {
            val sortedItems = screenItem.selectorItems.sortedBy { it.testElement.bounds.y1 }
            val first = sortedItems.firstOrNull() { it.keyInfo == "S" }
            if (first != null) {
                if (candidates.any() { it.nickname == first.nickname }.not()) {
                    candidates.add(first)
                }
            }
            val last = sortedItems.lastOrNull() { it.keyInfo == "S" }
            if (last != null) {
                if (candidates.any() { it.nickname == last.nickname }.not()) {
                    candidates.add(last)
                }
            }
        }
        return candidates.sortedWith(compareBy<SelectorItem> { it.screenItem?.fileNo?.toIntOrNull() }
            .thenBy { it.testElement.bounds.top })
    }

    fun refreshSatelliteCandidates() {

        if (satellitesViewModel.isAutoCheckBoxSelected.not()) {
            return
        }

        satellitesViewModel.items.clear()
        val candidates = getSatelliteCandidates()
        for (item in candidates) {
            satellitesViewModel.addItem(item)
        }
    }
}