import Image from "next/image";
import { API_BASE } from "@/lib/config";

/**
 * Avatar-komponent
 *
 * Viser en brukeravatar med fallback.
 * - Bruker Next.js <Image> for optimalisering.
 * - Hvis `src` starter med http/https, brukes den direkte.
 * - Hvis `src` er en relativ path (fra backend), prefixes med API_BASE.
 * - Hvis ingen `src` finnes, brukes en lokal standardavatar.
 */
export function Avatar({
  src,
  displayName,
  size = 40,
  className,
}: {
  src?: string | null;      
  displayName: string;     
  size?: number;            
  className?: string;       
}) {
  const finalSrc = src
    ? (src.startsWith("http://") || src.startsWith("https://"))
      ? src
      : `${API_BASE}${src}`  // prefiks API_BASE hvis relativ
    : "/avatars/_default.jpeg"; // fallback-bilde

  return (
    <Image
      src={finalSrc}
      alt={`${displayName} avatar`}  
      width={size}
      height={size}
      className={className ?? "rounded-full object-cover"}
    />
  );
}
