import { deleteUserAction, updateUserStatusAction } from "@/app/actions";
import { currentLanguage, safeBackendFetch } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import type { User } from "@/lib/types";

export default async function AdminCustomersPage() {
  const language = normalizeLanguage(await currentLanguage());
  const admin = dictionary(language).admin;
  const t = admin.customers;
  const users = await safeBackendFetch<User[]>("/api/users", [], { auth: true });

  return (
    <div className="stack">
      <div>
        <p className="muted">{t.eyebrow}</p>
        <h1 className="display text-5xl">{t.title}</h1>
      </div>
      <div className="table-wrap">
        <table>
          <thead><tr><th>{t.user}</th><th>{t.email}</th><th>{t.role}</th><th>{t.status}</th><th>{t.actions}</th></tr></thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.firstName} {user.lastName}<br /><span className="muted">{user.username}</span></td>
                <td>{user.email}</td>
                <td>{user.role}</td>
                <td><span className="status">{user.active ? admin.active : admin.passive}</span></td>
                <td className="flex flex-wrap gap-2">
                  {user.role === "ADMIN" ? (
                    <>
                      <button className="btn is-disabled" type="button" disabled aria-disabled="true">{user.active ? t.deactivate : t.activate}</button>
                      <button className="btn ghost is-disabled" type="button" disabled aria-disabled="true">{t.delete}</button>
                    </>
                  ) : (
                    <>
                      <form action={updateUserStatusAction}>
                        <input type="hidden" name="userId" value={user.id} />
                        <input type="hidden" name="userAction" value={user.active ? "deactivate" : "activate"} />
                        <button className="btn" type="submit">{user.active ? t.deactivate : t.activate}</button>
                      </form>
                      <form action={deleteUserAction}>
                        <input type="hidden" name="userId" value={user.id} />
                        <button className="btn ghost" type="submit">{t.delete}</button>
                      </form>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
