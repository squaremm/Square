package com.square.android.ui.fragment.fillProfileFirst

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.mukesh.countrypicker.Country
import com.mukesh.countrypicker.CountryPicker
import com.mukesh.countrypicker.listeners.OnCountryPickerListener
import com.square.android.R
import com.square.android.data.pojo.ProfileInfo
import com.square.android.extensions.content
import com.square.android.extensions.toOrdinalString
import com.square.android.presentation.presenter.fillProfileFirst.FillProfileFirstPresenter
import com.square.android.presentation.view.fillProfileFirst.FillProfileFirstView
import com.square.android.ui.dialogs.DatePickDialog
import com.square.android.ui.fragment.BaseFragment
import com.square.android.utils.ValidationCallback
import kotlinx.android.synthetic.main.fragment_fill_profile_1.*
import kotlinx.android.synthetic.main.profile_form_1.view.*
import org.jetbrains.anko.bundleOf
import java.util.*

private const val EXTRA_MODEL = "EXTRA_MODEL"
private const val COUNTRY_DEFAULT_ISO = "US"

class FillProfileFirstFragment : BaseFragment(), FillProfileFirstView,  ValidationCallback<CharSequence>, OnCountryPickerListener  {

    override fun showData(profileInfo: ProfileInfo) {
        form.formDialPhoneNumber.setText(profileInfo.phoneN)

        if(!TextUtils.isEmpty(profileInfo.phoneC)){
            form.formDialCode.text = profileInfo.phoneC
        }
        if(profileInfo.flagCode != -1){
            form.formDialFlag.setImageResource(profileInfo.flagCode)
        }

        form.formProfileName.setText(profileInfo.name)
        form.formProfileLastName.setText(profileInfo.surname)
        form.formProfileNationality.text = profileInfo.nationality
        form.formProfileBirth.text = profileInfo.displayBirthday
        form.formProfileGender.text = profileInfo.gender

        form.formProfileAccount.setText(profileInfo.instagramName)
    }

    companion object {

        @Suppress("DEPRECATION")
        fun newInstance(info: ProfileInfo): FillProfileFirstFragment {
            val fragment = FillProfileFirstFragment()

            val args = bundleOf(EXTRA_MODEL to info)
            fragment.arguments = args

            return fragment
        }
    }
    @InjectPresenter
    lateinit var presenter: FillProfileFirstPresenter

    @ProvidePresenter
    fun providePresenter(): FillProfileFirstPresenter = FillProfileFirstPresenter(getModel())

    private lateinit var countryPicker: CountryPicker

    private var fromDial: Boolean = false

    override fun showDialInfo(country: Country) {
        form.formDialCode.text = country.dialCode
        form.formDialFlag.setImageResource(country.flag)
    }

    private var dialog: SelectGenderDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.square.android.R.layout.fragment_fill_profile_1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        form.formDialPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        form.formDialCodeLl.setOnClickListener {showCountryDialDialog()}
        showDefaultDialInfo()

        form.formProfileBirth.setOnClickListener { showBirthDialog() }

        form.formProfileNationality.setOnClickListener { showCountryDialog() }

        form.formProfileGender.setOnClickListener { genderClicked() }

        fillProfile1Next.setOnClickListener { nextClicked() }

        setUpValidation()
    }

    private fun showDefaultDialInfo() {

        //TODO: change CountryPicker dialog design
        countryPicker = CountryPicker.Builder().with(activity!!)
                .listener(this)
                .build()

        val country = countryPicker.getCountryByISO(COUNTRY_DEFAULT_ISO)

        showDialInfo(country)
    }

    override fun displayNationality(country: Country) {
        form.formProfileNationality.text = country.name
    }

    private fun showCountryDialDialog() {
        activity?.let {
            fromDial = true
            countryPicker.showDialog(it)
        }
    }

    private fun showCountryDialog() {
        //TODO: change CountryPicker dialog design
        activity?.let {
            fromDial = false

            CountryPicker.Builder().with(it)
                    .listener(this)
                    .build()
                    .showDialog(it)
        }
    }

    private fun setUpValidation() {
        val validateList = listOf(
                form.formProfileNationality, form.formProfileBirth,
                form.formProfileGender
        )

        addTextValidation(validateList, this)
    }

    override fun validityChanged(isValid: Boolean) {
        fillProfile1Next.isEnabled = isValid
    }

    override fun onSelectCountry(country: Country) {
        if(!fromDial){
            presenter.countrySelected(country)
        } else{
            presenter.countryDialSelected(country)
        }
    }

    override fun isValid(item: CharSequence) = item.toString().trim().isNotEmpty()

    private fun nextClicked() {

        if(!isValid( form.formProfileName.content)){
            form.formProfileName.setText("")
            form.formProfileName.showCustomError(getString(R.string.name_error))
        }

        if(!isValid( form.formProfileLastName.content)){
            form.formProfileLastName.setText("")
            form.formProfileLastName.showCustomError(getString(R.string.last_name_error))
        }

        if(!isValid(form.formDialPhoneNumber.content)){
            form.formDialPhoneNumber.setText("")
            form.formDialPhoneNumber.showCustomError(getString(R.string.phone_error))
        }

        if(!isValid(form.formProfileAccount.content)){
            form.formProfileAccount.setText("")
            form.formProfileAccount.showCustomError(getString(R.string.username_error))
        }

        if(!form.formProfileName.errorShowing && !form.formProfileLastName.errorShowing && !form.formDialPhoneNumber.errorShowing && !form.formProfileAccount.errorShowing){
            val name = form.formProfileName.content
            val surname = form.formProfileLastName.content

            val phone = "${form.formDialCode.content} ${form.formDialPhoneNumber.content}"

            val phoneN = form.formDialPhoneNumber.content
            val phoneC = form.formDialCode.content

            val account = form.formProfileAccount.content

            presenter.nextClicked(name = name, surname = surname, phone = phone, phoneN = phoneN, phoneC = phoneC, account = account)
        }
    }

    private fun genderClicked(){
        context?.let {

            //TODO: change SelectGenderDialog layout to something better looking
            dialog = SelectGenderDialog(it)
            dialog!!.show(){
                when(it){
                    1 -> presenter.genderSelected(getString(R.string.gender_female))
                    2 -> presenter.genderSelected(getString(R.string.gender_male))
                }
            }
        }
    }

    override fun displayGender(gender: String) {
        form.formProfileGender.text = gender
    }

    override fun showBirthday(displayBirthday: String) {
        form.formProfileBirth.text = displayBirthday
    }

    private fun showBirthDialog() {
        DatePickDialog(activity!!)
                .show { calendar: Calendar ->
                    val mothName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                    val dayName = calendar.get(Calendar.DAY_OF_MONTH).toOrdinalString()
                    val modelBirthday = activity!!.getString(R.string.birthday_format, dayName, mothName)

                    val day = if (calendar.get(Calendar.DAY_OF_MONTH) < 10) "0" + calendar.get(Calendar.DAY_OF_MONTH).toString() else calendar.get(Calendar.DAY_OF_MONTH).toString()
                    val month = if ((calendar.get(Calendar.MONTH) + 1) < 10) "0" + (calendar.get(Calendar.MONTH) + 1).toString() else (calendar.get(Calendar.MONTH) + 1).toString()
                    val year = calendar.get(Calendar.YEAR).toString()

                    val displayBirthday = activity!!.getString(R.string.birthday_display_format, day, month, year)

                    presenter.birthSelected(modelBirthday, displayBirthday)
                }
    }

    private fun getModel(): ProfileInfo {
        return arguments?.getParcelable(EXTRA_MODEL) as ProfileInfo
    }

    override fun onStop() {
        val profileInfo = presenter.info

        if(isValid(form.formProfileName.content)){
          profileInfo.name = form.formProfileName.content
        }

        if(isValid(form.formProfileLastName.content)){
          profileInfo.surname =  form.formProfileLastName.content
        }

        if(isValid(form.formDialPhoneNumber.content)){
            profileInfo.phone = "${form.formDialCode.content} ${form.formDialPhoneNumber.content}"
        }

        if(isValid(form.formProfileAccount.content)){
            profileInfo.instagramName = form.formProfileAccount.content
        }

        profileInfo.phoneN = form.formDialPhoneNumber.content
        profileInfo.phoneC = form.formDialCode.content

        presenter.saveState(profileInfo, 1)

        super.onStop()
    }
}
