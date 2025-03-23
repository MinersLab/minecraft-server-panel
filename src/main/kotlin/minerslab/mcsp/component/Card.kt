package minerslab.mcsp.component

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.card.CardVariant
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.shared.HasThemeVariant
import minerslab.mcsp.util.slot

@Tag("vaadin-card")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "^24.7")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/card", version = "^24.7")
@JsModule("@vaadin/card/src/vaadin-card.js")
class Card : Component(), HasSize, HasThemeVariant<CardVariant>, HasComponents {

    var title by slot()
    var subtitle by slot()
    var media by slot()
    var headerPrefix by slot()
    var footer by slot()
    var headerSuffix by slot()
    var header by slot()

}