import {
  changePasswordAction,
  createAddressAction,
  deleteAddressAction,
  setDefaultAddressAction,
  updateAddressAction,
  updateProfileAction,
} from "@/app/actions";
import { currentLanguage, safeBackendFetch } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import { requireSession } from "@/lib/session";
import type { Address, User } from "@/lib/types";

type ProfilePageProps = {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
};

function firstParam(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

function notice(params: Awaited<ProfilePageProps["searchParams"]>, t: ReturnType<typeof dictionary>["profile"]) {
  if (firstParam(params.profileUpdated)) return t.updated;
  if (firstParam(params.passwordChanged)) return t.passwordChanged;
  if (firstParam(params.addressUpdated)) return t.addressUpdated;
  if (firstParam(params.addressDeleted)) return t.addressDeleted;
  return null;
}

export default async function ProfilePage({ searchParams }: ProfilePageProps) {
  const params = await searchParams;
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).profile;
  const session = await requireSession();
  const [user, addresses] = await Promise.all([
    safeBackendFetch<User>(`/api/users/${session.id}`, {
      id: session.id,
      username: session.username,
      email: session.email,
      role: session.role,
      firstName: session.firstName,
      lastName: session.lastName,
      active: true,
      emailVerified: true,
    }, { auth: true }),
    safeBackendFetch<Address[]>(`/api/users/${session.id}/addresses`, [], { auth: true }),
  ]);
  const success = notice(params, t);
  const profileError = firstParam(params.profileError);

  return (
    <div className="page-shell section">
      <p className="status">{t.eyebrow}</p>
      <h1 className="display mt-3 text-5xl">{t.title}</h1>
      {success ? <div className="notice success"><span>{success}</span></div> : null}
      {profileError ? (
        <div className="notice error">
          <strong>{t.profileError}</strong>
          <span>{profileError}</span>
        </div>
      ) : null}

      <div className="profile-grid mt-8">
        <section className="panel p-6">
          <h2 className="display text-3xl">{t.personalInfo}</h2>
          <form action={updateProfileAction} className="form-grid mt-5">
            <input type="hidden" name="userId" value={session.id} />
            <input type="hidden" name="redirectTo" value="/profile" />
            <label className="field"><span>{t.username}</span><input className="input" value={user.username} disabled /></label>
            <label className="field"><span>{t.email}</span><input className="input" name="email" type="email" defaultValue={user.email} required /></label>
            <label className="field"><span>{t.firstName}</span><input className="input" name="firstName" defaultValue={user.firstName ?? ""} /></label>
            <label className="field"><span>{t.lastName}</span><input className="input" name="lastName" defaultValue={user.lastName ?? ""} /></label>
            <label className="field md:col-span-2"><span>{t.phone}</span><input className="input" name="phoneNumber" defaultValue={user.phoneNumber ?? ""} /></label>
            <button className="btn primary" type="submit">{t.saveProfile}</button>
          </form>
        </section>

        <section className="panel p-6">
          <h2 className="display text-3xl">{t.password}</h2>
          <form action={changePasswordAction} className="stack mt-5">
            <input type="hidden" name="userId" value={session.id} />
            <input type="hidden" name="redirectTo" value="/profile" />
            <label className="field"><span>{t.currentPassword}</span><input className="input" name="currentPassword" type="password" required /></label>
            <label className="field"><span>{t.newPassword}</span><input className="input" name="newPassword" type="password" minLength={6} required /></label>
            <button className="btn primary" type="submit">{t.changePassword}</button>
          </form>
        </section>
      </div>

      <section className="section">
        <div className="toolbar">
          <h2 className="display text-4xl">{t.addresses}</h2>
          <span className="muted">{addresses.length}</span>
        </div>
        {addresses.length === 0 ? <p className="muted">{t.noAddresses}</p> : null}
        <div className="address-grid">
          {addresses.map((address) => (
            <article className="panel p-5" key={address.id}>
              <form action={updateAddressAction} className="form-grid">
                <input type="hidden" name="userId" value={session.id} />
                <input type="hidden" name="addressId" value={address.id} />
                <label className="field"><span>{t.titleField}</span><input className="input" name="title" defaultValue={address.title} required /></label>
                <label className="field"><span>{t.city}</span><input className="input" name="city" defaultValue={address.city} required /></label>
                <label className="field md:col-span-2"><span>{t.street}</span><input className="input" name="street" defaultValue={address.street} required /></label>
                <label className="field"><span>{t.country}</span><input className="input" name="country" defaultValue={address.country} required /></label>
                <label className="field"><span>{t.zip}</span><input className="input" name="zipCode" defaultValue={address.zipCode} required /></label>
                <label className="flex items-center gap-2"><input name="defaultAddress" type="checkbox" defaultChecked={address.defaultAddress} /> {t.default}</label>
                <button className="btn" type="submit">{t.updateAddress}</button>
              </form>
              <div className="mt-3 flex flex-wrap gap-2">
                {!address.defaultAddress ? (
                  <form action={setDefaultAddressAction}>
                    <input type="hidden" name="userId" value={session.id} />
                    <input type="hidden" name="addressId" value={address.id} />
                    <button className="btn ghost" type="submit">{t.setDefault}</button>
                  </form>
                ) : null}
                <form action={deleteAddressAction}>
                  <input type="hidden" name="userId" value={session.id} />
                  <input type="hidden" name="addressId" value={address.id} />
                  <button className="btn ghost" type="submit">{t.delete}</button>
                </form>
              </div>
            </article>
          ))}
        </div>

        <div className="panel p-6 mt-6">
          <h3 className="display text-3xl">{t.addAddress}</h3>
          <form action={createAddressAction} className="form-grid mt-5">
            <input type="hidden" name="userId" value={session.id} />
            <input type="hidden" name="redirectTo" value="/profile" />
            <label className="field"><span>{t.titleField}</span><input className="input" name="title" required /></label>
            <label className="field"><span>{t.city}</span><input className="input" name="city" required /></label>
            <label className="field"><span>{t.street}</span><input className="input" name="street" required /></label>
            <label className="field"><span>{t.country}</span><input className="input" name="country" defaultValue={language === "tr" ? "Turkiye" : "Turkey"} required /></label>
            <label className="field"><span>{t.zip}</span><input className="input" name="zipCode" required /></label>
            <label className="flex items-center gap-2 pt-7"><input name="defaultAddress" type="checkbox" /> {t.default}</label>
            <button className="btn primary" type="submit">{t.saveAddress}</button>
          </form>
        </div>
      </section>
    </div>
  );
}
