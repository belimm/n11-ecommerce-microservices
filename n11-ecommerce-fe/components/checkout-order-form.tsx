"use client";

import { useMemo, useState } from "react";
import { createAddressAction, createOrderAction } from "@/app/actions";
import type { Language } from "@/lib/i18n";
import type { Address } from "@/lib/types";

const ADD_NEW_ADDRESS = "__add_new_address__";

type TestCard = {
  value: string;
  tr: string;
  en: string;
};

type CheckoutText = {
  shipping: string;
  selectAddress: string;
  addNewAddress: string;
  default: string;
  payment: string;
  testCard: string;
  holder: string;
  expiryMonth: string;
  expiryYear: string;
  cvc: string;
  pay: string;
  addAddress: string;
  titleField: string;
  city: string;
  street: string;
  country: string;
  zip: string;
  saveAddress: string;
  noAddressTitle: string;
  noAddressMessage: string;
  noAddressSelect: string;
  paymentLocked: string;
  paymentReady: string;
  incompletePayment: string;
  emptyCart: string;
};

type CheckoutOrderFormProps = {
  addresses: Address[];
  hasCartItems: boolean;
  initialAddressId?: string;
  language: Language;
  sessionId: string;
  testCards: TestCard[];
  text: CheckoutText;
};

export function CheckoutOrderForm({ addresses, hasCartItems, initialAddressId, language, sessionId, testCards, text }: CheckoutOrderFormProps) {
  const initialSelectedAddress = addresses.some((address) => address.id === initialAddressId)
    ? initialAddressId ?? ""
    : addresses[0]?.id ?? ADD_NEW_ADDRESS;
  const [addressId, setAddressId] = useState(initialSelectedAddress);
  const [cardNumber, setCardNumber] = useState(testCards[0]?.value ?? "");
  const [cardHolderName, setCardHolderName] = useState("John Doe");
  const [expireMonth, setExpireMonth] = useState("12");
  const [expireYear, setExpireYear] = useState("2030");
  const [cvc, setCvc] = useState("123");

  const hasAddress = addresses.length > 0;
  const isAddingAddress = addressId === ADD_NEW_ADDRESS;
  const hasSelectedAddress = hasAddress && !isAddingAddress && Boolean(addressId);
  const paymentComplete = useMemo(
    () => [cardNumber, cardHolderName, expireMonth, expireYear, cvc].every((field) => field.trim().length > 0),
    [cardNumber, cardHolderName, expireMonth, expireYear, cvc],
  );
  const canSubmit = hasCartItems && hasSelectedAddress && paymentComplete;
  const statusMessage = !hasCartItems
    ? text.emptyCart
    : !hasSelectedAddress
      ? text.noAddressMessage
      : paymentComplete
        ? text.paymentReady
        : text.incompletePayment;

  return (
    <div className="panel p-6">
      <h2 className="display text-3xl">{text.shipping}</h2>
      {!hasSelectedAddress ? (
        <div className="notice mt-5">
          <strong>{text.noAddressTitle}</strong>
          <span>{text.noAddressMessage}</span>
        </div>
      ) : null}

      <form action={createOrderAction} className="checkout-flow mt-5">
        <label className="field">
          <span>{text.selectAddress}</span>
          <select
            className="input"
            name="addressId"
            value={addressId}
            onChange={(event) => setAddressId(event.target.value)}
            required
          >
            {!hasAddress ? <option value={ADD_NEW_ADDRESS}>{text.noAddressSelect}</option> : null}
            {addresses.map((address) => (
              <option key={address.id} value={address.id}>
                {address.title} - {address.city} {address.defaultAddress ? `(${text.default})` : ""}
              </option>
            ))}
            <option value={ADD_NEW_ADDRESS}>{text.addNewAddress}</option>
          </select>
        </label>

        <fieldset className={`payment-step ${!hasSelectedAddress ? "is-locked" : ""}`} disabled={!hasSelectedAddress} aria-disabled={!hasSelectedAddress}>
          <legend className="display text-2xl">{text.payment}</legend>
          {!hasSelectedAddress ? <p className="muted mt-2">{text.paymentLocked}</p> : null}
          <div className="form-grid mt-4">
            <label className="field">
              <span>{text.testCard}</span>
              <select className="input" name="cardNumber" value={cardNumber} onChange={(event) => setCardNumber(event.target.value)} required>
                {testCards.map((card) => (
                  <option key={card.value} value={card.value}>
                    {card.value} - {card[language]}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              <span>{text.holder}</span>
              <input className="input" name="cardHolderName" value={cardHolderName} onChange={(event) => setCardHolderName(event.target.value)} required />
            </label>
            <label className="field">
              <span>{text.expiryMonth}</span>
              <input className="input" name="expireMonth" inputMode="numeric" maxLength={2} value={expireMonth} onChange={(event) => setExpireMonth(event.target.value)} required />
            </label>
            <label className="field">
              <span>{text.expiryYear}</span>
              <input className="input" name="expireYear" inputMode="numeric" maxLength={4} value={expireYear} onChange={(event) => setExpireYear(event.target.value)} required />
            </label>
            <label className="field">
              <span>{text.cvc}</span>
              <input className="input" name="cvc" inputMode="numeric" maxLength={4} value={cvc} onChange={(event) => setCvc(event.target.value)} required />
            </label>
          </div>
        </fieldset>

        <div className="checkout-submit">
          <p className="muted">{statusMessage}</p>
          <button className="btn primary" type="submit" disabled={!canSubmit}>
            {text.pay}
          </button>
        </div>
      </form>

      {isAddingAddress ? (
        <div className="address-inline-form mt-6">
          <h3 className="display text-2xl">{text.addAddress}</h3>
          <form action={createAddressAction} className="form-grid mt-4">
            <input type="hidden" name="userId" value={sessionId} />
            <input type="hidden" name="redirectTo" value="/checkout" />
            <label className="field"><span>{text.titleField}</span><input className="input" name="title" required /></label>
            <label className="field"><span>{text.city}</span><input className="input" name="city" required /></label>
            <label className="field"><span>{text.street}</span><input className="input" name="street" required /></label>
            <label className="field"><span>{text.country}</span><input className="input" name="country" defaultValue={language === "tr" ? "Turkiye" : "Turkey"} required /></label>
            <label className="field"><span>{text.zip}</span><input className="input" name="zipCode" required /></label>
            <label className="flex items-center gap-2 pt-7"><input name="defaultAddress" type="checkbox" /> {text.default}</label>
            <button className="btn" type="submit">{text.saveAddress}</button>
          </form>
        </div>
      ) : null}
    </div>
  );
}
