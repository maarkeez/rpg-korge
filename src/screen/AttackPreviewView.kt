package screen

import battleunit.domain.*
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import unit.domain.*

class AttackPreviewView: Container() {

    private lateinit var casterUnitNameView: UnitNameView
    private lateinit var casterUnitPortraitView: UnitPortraitView
    private lateinit var casterHealthBarView: HealthBarView
    private lateinit var casterManaBarView: ManaBarPreviewView

    private lateinit var receiverUnitNameView: UnitNameView
    private lateinit var receiverUnitPortraitView: UnitPortraitView
    private lateinit var receiverHealthBarView: HealthBarPreviewView
    private lateinit var receiverManaBarView: ManaBarView

    private lateinit var cancelButton: UIButton
    private lateinit var confirmButton: UIButton

    private lateinit var delegate: Delegate

    suspend fun loadAssets() {
        casterUnitPortraitView.loadAssets()
        receiverUnitPortraitView.loadAssets()
    }

    private val layout = uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                casterUnitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                addChild(casterUnitPortraitView)

                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    casterUnitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                    addChild(casterUnitNameView)

                    uiSpacing(Size(0, 15))
                    casterHealthBarView =  HealthBarView(Size(281.5, 16))
                    addChild(casterHealthBarView)

                    uiSpacing(Size(0, 4))
                    casterManaBarView = ManaBarPreviewView(Size(281.5, 16))
                    addChild(casterManaBarView)
                }
            }
            uiSpacing(Size(0, 10))
            uiHorizontalStack {
                uiVerticalStack(padding = 1.0) {
                    receiverUnitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                    receiverUnitNameView.alignTextToRight()
                    addChild(receiverUnitNameView)

                    uiSpacing(Size(0, 15))
                    receiverHealthBarView =  HealthBarPreviewView(Size(281.5, 16))
                    addChild(receiverHealthBarView)

                    uiSpacing(Size(0, 4))
                    receiverManaBarView = ManaBarView(Size(281.5, 16))
                    addChild(receiverManaBarView)
                }
                uiSpacing(Size(10, 0))
                receiverUnitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                addChild(receiverUnitPortraitView)

            }
            uiHorizontalStack {
                cancelButton = uiButton {
                    text = "Cancel"
                    width = 190.0
                    bgColorOut = Colors.DIMGRAY
                    bgColorOver = Colors.DIMGRAY
                    background.borderColor = Colors.LIGHTGRAY
                    onClick {
                        delegate.castCancelled()
                    }
                }
                uiSpacing(Size(10, 0))
                confirmButton = uiButton {
                    text = "Confirm"
                    width = 190.0
                    bgColorOut = Colors.DIMGRAY
                    bgColorOver = Colors.DIMGRAY
                    background.borderColor = Colors.LIGHTGRAY
                    onClick {
                        delegate.castConfirmed()
                    }
                }
            }
        }


    init {
        visible = false
        addChild(layout)
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun display(
        casterBattleUnit: BattleUnit.Dto,
        casterUnit: Unit.Dto,
        manaCost: Int,
        receiverBattleUnit: BattleUnit.Dto?,
        receiverUnit: Unit.Dto?,
        damage: Int?,
    ) {
        casterUnitPortraitView.display(casterBattleUnit.unitId)
        casterUnitNameView.display(unitName = casterUnit.name)
        casterHealthBarView.display(
            remaining = casterBattleUnit.remainingHealthPoints,
            maximum = casterUnit.healthPoints,
        )
        casterManaBarView.display(
            remainingBefore = casterBattleUnit.remainingManaPoints,
            remainingAfter = casterBattleUnit.remainingManaPoints - manaCost,
            maximum = casterUnit.manaPoints,
        )

        if(receiverBattleUnit == null || receiverUnit == null || damage == null) {
            receiverUnitPortraitView.hide()
            receiverUnitNameView.hide()
            receiverHealthBarView.hide()
            receiverManaBarView.hide()
        }else{
            receiverUnitPortraitView.display(receiverBattleUnit.unitId)
            receiverUnitNameView.display(unitName = receiverUnit.name)
            receiverHealthBarView.display(
                remainingBefore = receiverBattleUnit.remainingHealthPoints,
                remainingAfter = receiverBattleUnit.remainingHealthPoints - damage,
                maximum = receiverUnit.healthPoints,
            )
            receiverManaBarView.display(
                remaining = receiverBattleUnit.remainingManaPoints,
                maximum = receiverUnit.manaPoints,
            )
        }
        visible = true
    }

    fun hide() {
        visible = false
    }

    interface Delegate {
        fun castConfirmed()
        fun castCancelled()
    }
}
