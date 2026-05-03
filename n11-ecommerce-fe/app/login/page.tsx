import { loginAction, signupAction } from "@/app/actions";
import { currentLanguage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";

type LoginPageProps = {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
};

export default async function LoginPage({ searchParams }: LoginPageProps) {
  const params = await searchParams;
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).login;
  const error = Array.isArray(params.error) ? params.error[0] : params.error;

  return (
    <div className="page-shell section">
      <div className="hero-band">
        <div>
          <p className="status">{t.status}</p>
          <h1 className="display hero-title">{t.title}</h1>
          <p className="hero-copy">{t.copy}</p>
        </div>
        <div className="panel p-6">
          {error ? <p className="mb-4 text-red-800">{decodeURIComponent(error)}</p> : null}
          {params.registered ? <p className="mb-4 text-green-900">{t.registered}</p> : null}
          <form action={loginAction} className="stack">
            <label className="field"><span>{t.usernameOrEmail}</span><input className="input" name="usernameOrEmail" required /></label>
            <label className="field"><span>{t.password}</span><input className="input" name="password" type="password" required /></label>
            <button className="btn primary" type="submit">{t.login}</button>
          </form>
        </div>
      </div>
      <section className="panel p-6">
        <h2 className="display text-3xl">{t.createTitle}</h2>
        <form action={signupAction} className="form-grid mt-5">
          <label className="field"><span>{t.username}</span><input className="input" name="username" required /></label>
          <label className="field"><span>{t.email}</span><input className="input" name="email" type="email" required /></label>
          <label className="field"><span>{t.password}</span><input className="input" name="password" type="password" required /></label>
          <label className="field"><span>{t.phone}</span><input className="input" name="phoneNumber" /></label>
          <label className="field"><span>{t.firstName}</span><input className="input" name="firstName" /></label>
          <label className="field"><span>{t.lastName}</span><input className="input" name="lastName" /></label>
          <button className="btn" type="submit">{t.create}</button>
        </form>
      </section>
    </div>
  );
}
