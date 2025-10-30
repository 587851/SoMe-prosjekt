import "./globals.css";
import Image from "next/image";
import Link from "next/link";
import ThemeToggle from "../components/ThemeToggle";
import HeaderAuth from "../components/HeaderAuth";
import UserButton from "../components/UserButton";
import IfLoggedIn from "../components/IfLoggedIn";
import SearchBar from "../components/SearchUsers"; 

/**
 * RootLayout er toppnivå-layouten i Next.js (App Router).
 * - Laster inn globale stiler (globals.css)
 * - Viser fast header med logo, søk, auth-knapper og tema-bryter
 * - Renderer resten av appen i <main>
 */
export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="no">
      <body>
        {/* Global header */}
        <header className="site-header">
          <div className="header-inner">
            
            {/* Venstre side: logo + brand */}
            <div className="header-left">
              <Link href="/" className="brand-link">
                <Image src="/logo.svg" alt="Logo" width={40} height={40} />
              </Link>
              <h1 className="brand">SoMe</h1>
            </div>

            {/* Midten: søkefelt, vises kun når bruker er innlogget */}
            <IfLoggedIn>
              <div className="header-center">
                <SearchBar />
              </div>
            </IfLoggedIn>

            {/* Høyre side: brukerknapp, tema-toggle og auth-links */}
            <div className="header-right">
              <IfLoggedIn>
                <UserButton /> {/* Profil/meny for innlogget bruker */}
              </IfLoggedIn>
              <ThemeToggle />   {/* Mørk/Lys modus switch */}
              <HeaderAuth />    {/* Login/Register eller Logout-knapper */}
            </div>

          </div>
        </header>

        {/* Appens faktiske sider rendres her */}
        <main>{children}</main>
      </body>
    </html>
  );
}
