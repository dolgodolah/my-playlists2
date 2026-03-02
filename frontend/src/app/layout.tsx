import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "내플리스",
  description: "My Playlists",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
